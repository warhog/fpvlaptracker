import {NgModule, ErrorHandler} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {IonicApp, IonicModule, IonicErrorHandler} from 'ionic-angular';
import {FpvlaptrackerApp} from './app.component';
import {HttpClientModule} from '@angular/common/http';

import {BluetoothPage} from '../pages/bluetooth/bluetooth';
import {SettingsPage} from '../pages/settings/settings';
import {ScannerPage} from '../pages/scanner/scanner';
import {RacePage} from '../pages/race/race';
import {DevicePage} from '../pages/device/device';
import {HomePage} from '../pages/home/home';
import {TabsPage} from '../pages/tabs/tabs';
import {FastrssiPage} from '../pages/fastrssi/fastrssi';

import {StatusBar} from '@ionic-native/status-bar';
import {SplashScreen} from '@ionic-native/splash-screen';

import {BluetoothSerial} from '@ionic-native/bluetooth-serial';
import {IonicStorageModule} from '@ionic/storage';
import {SmartAudioProvider} from '../providers/smart-audio/smart-audio';
import {SpeechProvider} from '../providers/speech/speech'
import {TextToSpeech} from '@ionic-native/text-to-speech'
import {NativeAudio} from '@ionic-native/native-audio';
import {Insomnia} from '@ionic-native/insomnia';
import {FltutilProvider} from '../providers/fltutil/fltutil';
import {FltunitProvider} from '../providers/fltunit/fltunit';
import { HelpPage } from '../pages/help/help';

@NgModule({
    declarations: [
        FpvlaptrackerApp,
        BluetoothPage,
        SettingsPage,
        ScannerPage,
        DevicePage,
        RacePage,
        HomePage,
        TabsPage,
        FastrssiPage,
        HelpPage
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        IonicModule.forRoot(FpvlaptrackerApp),
        IonicStorageModule.forRoot()
    ],
    bootstrap: [IonicApp],
    entryComponents: [
        FpvlaptrackerApp,
        BluetoothPage,
        SettingsPage,
        ScannerPage,
        DevicePage,
        RacePage,
        HomePage,
        TabsPage,
        FastrssiPage,
        HelpPage
    ],
    providers: [
        StatusBar,
        SplashScreen,
        BluetoothSerial,
        {provide: ErrorHandler, useClass: IonicErrorHandler},
        NativeAudio,
        SmartAudioProvider,
        TextToSpeech,
        SpeechProvider,
        Insomnia,
        FltutilProvider,
        FltunitProvider,
        FltunitProvider
    ]
})
export class AppModule {}
