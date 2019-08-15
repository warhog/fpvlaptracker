// taken from https://www.c-sharpcorner.com/article/confirm-dialog-in-angular-using-bootstrap-modal/
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface ConfirmDialogData {
    type: string;
    text: string;
    title: string;
    yesFn: Function;
    noFn: Function;
}

@Injectable({
    providedIn: 'root'
})
export class ConfirmDialogService {
    private subject: Subject<ConfirmDialogData> = new Subject<ConfirmDialogData>();
    
    constructor() { }

    confirmYesNo(title: string, message: string, yesFn: () => void, noFn: () => void) {
        let me = this;
        this.subject.next({
            type: 'confirmyesno',
            text: message,
            title: title,
            yesFn: function () {
                me.subject.next();
                yesFn();
            },
            noFn: function () {
                me.subject.next();
                noFn();
            }
        });
    }

    confirmOK(title: string, message: string, okFn: () => void) {
        let me = this;
        this.subject.next({
            type: 'confirmok',
            text: message,
            title: title,
            yesFn: function () {
                me.subject.next();
                okFn();
            },
            noFn: function () {
                me.subject.next();
            }
        });
    }

    getMessage(): Observable<any> {
        return this.subject.asObservable();
    }
}  