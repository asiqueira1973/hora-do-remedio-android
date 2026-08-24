// App.kt — classe Application (épico 8.0). Ponto único de criação do banco e do repositório,
// reaproveitados por toda a sessão (equivalente ao useLiveQuery/client.ts do iOS).

package com.dablab.horadoremedio

import android.app.Application
import com.dablab.horadoremedio.dados.AppDatabase
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.lembretes.NotificationHelper

class App : Application() {
    val database: AppDatabase by lazy { AppDatabase.obter(this) }
    val repositorio: Repositorio by lazy { Repositorio(database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.criarCanal(this)
    }
}
