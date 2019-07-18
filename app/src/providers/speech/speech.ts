import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';
import { TextToSpeech } from '@ionic-native/text-to-speech';

/*
  Generated class for the SpeechProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular DI.
*/
@Injectable()
export class SpeechProvider {

    constructor(private tts: TextToSpeech) {

    }

    speak(text: string) {
        this.tts.speak({
            text: text,
            locale: 'en-US',
            rate: 1.0
        }).catch((reason: any) => {
                console.log('failed to speak', reason);
            });
    }

}
