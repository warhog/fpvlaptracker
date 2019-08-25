import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TabsPage } from './tabs';
import { DevicePage } from '../device/device';
import { HomePage } from '../home/home.page';
import { RacePage } from '../race/race';
import { SettingsPage } from '../settings/settings';
import { HelpPage } from '../help/help';
import { ScannerPage } from '../scanner/scanner';
import { BluetoothPage } from '../bluetooth/bluetooth';

const routes: Routes = [
    {
        path: 'tabs',
        component: TabsPage,
        children: [
            { path: 'home', children: [{ path: '', component: HomePage }] },
            { path: 'device', children: [{ path: '', component: DevicePage }] },
            { path: 'race', children: [{ path: '', component: RacePage }] },
            { path: 'settings', children: [{ path: '', component: SettingsPage }] },
            { path: 'help', children: [{ path: '', component: HelpPage }] },
            { path: 'scanner', children: [{ path: '', component: ScannerPage }] },
            { path: 'bluetooth', children: [{ path: '', component: BluetoothPage }] }
        ]
    }, {
        path: '',
        redirectTo: '/tabs/home',
        pathMatch: 'full'
    }
]

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class TabsPageRoutingModule { }