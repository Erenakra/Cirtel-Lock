package dev.cirtellock.applock.features.applist.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cirtellock.applock.data.repository.AppLockRepository
import dev.cirtellock.applock.features.applist.domain.AppSearchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appSearchManager = AppSearchManager(application)
    private val appLockRepository = AppLockRepository(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allApps = MutableStateFlow<Set<ApplicationInfo>>(emptySet())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())

    private val _debouncedQuery = MutableStateFlow("")

    val lockedAppsFlow: StateFlow<List<ApplicationInfo>> =
        combine(_allApps, _lockedApps, _debouncedQuery) { apps, locked, query ->
            apps.filter { it.packageName in locked }
                .filter { it.matchesQuery(query) }
                .sortedBy { it.loadLabel(getApplication<Application>().packageManager).toString() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val unlockedAppsFlow: StateFlow<List<ApplicationInfo>> =
        combine(_allApps, _lockedApps, _debouncedQuery) { apps, locked, query ->
            apps.filterNot { it.packageName in locked }
                .filter { it.matchesQuery(query) }
                .sortedBy { it.loadLabel(getApplication<Application>().packageManager).toString() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private fun ApplicationInfo.matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        return loadLabel(getApplication<Application>().packageManager).toString()
            .contains(query, ignoreCase = true)
    }

    init {
        loadAllApplications()
        loadLockedApps()

        viewModelScope.launch {
            _searchQuery
                .debounce(100L)
                .collect { query ->
                    _debouncedQuery.value = query
                }
        }
    }

    private fun loadAllApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apps = withContext(Dispatchers.IO) {
                    appSearchManager.loadApps(true)
                }
                _allApps.value = apps

                // Perform initial auto-lock if not done yet
                if (!appLockRepository.isInitialAutoLockDone()) {
                    val targetPackages = setOf("com.microsoft.office.outlook", "com.nextcloud.client")
                    val foundPackages = apps.filter { it.packageName in targetPackages }.map { it.packageName }

                    if (foundPackages.isNotEmpty()) {
                        appLockRepository.addMultipleLockedApps(foundPackages.toSet())
                        loadLockedApps()
                    }
                    appLockRepository.setInitialAutoLockDone(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _allApps.value = emptySet()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLockedApps() {
        _lockedApps.value = appLockRepository.getLockedApps()
    }

    fun lockApps(packageNames: List<String>) {
        appLockRepository.addMultipleLockedApps(packageNames.toSet())
        _lockedApps.value = appLockRepository.getLockedApps()
    }

    fun unlockApp(packageName: String) {
        appLockRepository.removeLockedApp(packageName)
        _lockedApps.value = appLockRepository.getLockedApps()
    }
}
