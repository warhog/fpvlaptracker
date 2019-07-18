import {Component} from '@angular/core';
import {NavController, ViewController} from 'ionic-angular';

@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {

    constructor(public navCtrl: NavController, private viewCtrl: ViewController) {

    }

    ionViewWillEnter() {
        this.viewCtrl.showBackButton(false);
    }

}
