import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { FormsModule } from '@angular/forms';
import { NativeAudio } from '@ionic-native/native-audio/ngx';
import { IonicStorageModule } from '@ionic/storage';
import { BluetoothSerial } from '@ionic-native/bluetooth-serial/ngx';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';
import { Insomnia } from '@ionic-native/insomnia/ngx';
import { SmartAudioProvider } from './services/smart-audio/smart-audio';
import { SpeechProvider } from './services/speech/speech';

@NgModule({
  declarations: [
    AppComponent
  ],
  entryComponents: [],
  imports: [
    BrowserModule, 
    IonicModule.forRoot(), 
    AppRoutingModule,
    FormsModule,
    IonicStorageModule.forRoot()
  ],
  providers: [
    StatusBar,
    SplashScreen,
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    NativeAudio,
    BluetoothSerial,
    TextToSpeech,
    Insomnia,
    SmartAudioProvider,
    SpeechProvider
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
