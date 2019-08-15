import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { HomeComponent } from './home/home.component'
import { LoginComponent } from './login/login.component'
import { NodesetupComponent } from './nodesetup/nodesetup.component'
import { RaceComponent } from './race/race.component'
import { SettingsComponent } from './settings/settings.component';
import { PilotsComponent } from './pilots/pilots.component';
import { NodesComponent } from './nodes/nodes.component';
import { ScanComponent } from './scan/scan.component';
import { PilotsetupComponent } from './pilotsetup/pilotsetup.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'login/:path', component: LoginComponent },
  { path: 'nodesetup/:id', component: NodesetupComponent },
  { path: 'pilotsetup/:name', component: PilotsetupComponent },
  { path: 'scan/:id', component: ScanComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'pilots', component: PilotsComponent },
  { path: 'nodes', component: NodesComponent },
  { path: 'race', component: RaceComponent },
  { path: '', pathMatch: 'full', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
