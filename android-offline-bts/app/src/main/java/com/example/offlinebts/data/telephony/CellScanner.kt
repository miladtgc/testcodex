package com.example.offlinebts.data.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager

data class SeenCell(
    val radio: String,
    val cellId: Long,
    val rsrpDbm: Int?,
    val isRegistered: Boolean
)

class CellScanner(context: Context) {

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @SuppressLint("MissingPermission")
    fun getVisibleCells(): List<SeenCell> {
        val infos: List<CellInfo> = telephonyManager.allCellInfo ?: emptyList()
        return infos.mapNotNull { info ->
            when (info) {
                is CellInfoLte -> {
                    val ci = info.cellIdentity.ci?.toLong() ?: return@mapNotNull null
                    val rsrp = if (Build.VERSION.SDK_INT >= 29) info.cellSignalStrength.rsrp else null
                    SeenCell("LTE", ci, rsrp, info.isRegistered)
                }

                is CellInfoWcdma -> {
                    val cid = info.cellIdentity.cid?.toLong() ?: return@mapNotNull null
                    SeenCell("WCDMA", cid, null, info.isRegistered)
                }

                is CellInfoGsm -> {
                    val cid = info.cellIdentity.cid?.toLong() ?: return@mapNotNull null
                    SeenCell("GSM", cid, null, info.isRegistered)
                }

                else -> null
            }
        }
    }
}
