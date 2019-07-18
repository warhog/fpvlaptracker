import {Component} from '@angular/core';
import {Platform} from 'ionic-angular';
import {StatusBar} from '@ionic-native/status-bar';
import {SplashScreen} from '@ionic-native/splash-screen';
import {SmartAudioProvider} from '../providers/smart-audio/smart-audio';

import {TabsPage} from '../pages/tabs/tabs';

@Component({
    templateUrl: 'app.html'
})
export class FpvlaptrackerApp {
    rootPage: any = TabsPage;

    constructor(platform: Platform, statusBar: StatusBar, splashScreen: SplashScreen, smartAudio: SmartAudioProvider) {
        platform.ready().then(() => {
            // Okay, so the platform is ready and our plugins are available.
            // Here you can do any higher level native things you might need.
            statusBar.styleDefault();

            smartAudio.preload('lap', 'assets/audio/lap.mp3');

            splashScreen.hide();
        });
    }
}
