package net.axay.pacmc.app

import co.touchlab.kermit.Logger
import net.axay.pacmc.app.database.realm

object CommonApplication {
    var openedRealm = false

    fun init() {
        Logger.setTag("pacmc")
    }

    fun close() {
        if (openedRealm) {
            realm.close()
        }
    }
}
