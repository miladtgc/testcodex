package com.example.offlinebts.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.offlinebts.data.location.LocationValidator
import com.example.offlinebts.data.telephony.CellScanner
import com.example.offlinebts.domain.PositionEstimator
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.renderer.TileRendererLayer
import java.io.File

@SuppressLint("MissingPermission")
@Composable
fun OfflineMapScreen(
    cellScanner: CellScanner,
    locationValidator: LocationValidator,
    positionEstimator: PositionEstimator
) {
    val context = LocalContext.current
    val status = remember { mutableStateOf("در حال بارگذاری...") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = status.value)

        AndroidView(
            factory = {
                AndroidGraphicFactory.createInstance(it.application)
                val mapView = MapView(it)
                mapView.setBuiltInZoomControls(true)
                mapView.model.mapViewPosition.zoomLevel = 16
                mapView.model.mapViewPosition.center = LatLong(35.7000, 51.4000)
                attachOfflineMap(it, mapView)

                val visible = cellScanner.getVisibleCells()
                val estimated = positionEstimator.estimateFromCells(visible)
                status.value = if (estimated == null) {
                    "BTS قابل استفاده برای تخمین کافی نیست (حداقل ۲ سلول LTE با RSRP لازم است)."
                } else {
                    "تخمین آفلاین از BTS: ${estimated.lat}, ${estimated.lon} | cells=${estimated.usedCellIds}"
                }
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun attachOfflineMap(context: Context, mapView: MapView) {
    val mapFile = File(context.filesDir, "iran-neighborhood.map")
    if (!mapFile.exists()) return

    val tileCache: TileCache = AndroidUtil.createTileCache(
        context,
        "offline-map-cache",
        mapView.model.displayModel.tileSize,
        1f,
        mapView.model.frameBufferModel.overdrawFactor
    )

    val renderer = TileRendererLayer(
        tileCache,
        AndroidUtil.createTileRendererLayer(
            tileCache,
            mapView.model.mapViewPosition,
            mapFile,
            AndroidGraphicFactory.INSTANCE
        ).mapDataStore,
        mapView.model.mapViewPosition,
        false,
        true,
        false,
        AndroidGraphicFactory.INSTANCE
    )

    mapView.layerManager.layers.add(renderer)
}
