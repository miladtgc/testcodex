package com.example.offlinebts

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.offlinebts.data.cell.LocalCellDatabase
import com.example.offlinebts.data.location.LocationValidator
import com.example.offlinebts.data.telephony.CellScanner
import com.example.offlinebts.domain.PositionEstimator
import com.example.offlinebts.ui.map.OfflineMapScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
        )

        val cellDatabase = LocalCellDatabase.seedNeighborhoodCells()
        val scanner = CellScanner(this)
        val validator = LocationValidator()
        val estimator = PositionEstimator(cellDatabase)

        setContent {
            MaterialTheme {
                Surface {
                    OfflineMapScreen(
                        cellScanner = scanner,
                        locationValidator = validator,
                        positionEstimator = estimator
                    )
                }
            }
        }
    }
}
