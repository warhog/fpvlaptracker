import { Component, OnInit } from '@angular/core';
import { ConfirmDialogService } from './confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent implements OnInit {
  private _message: any;

  constructor(private confirmDialogService: ConfirmDialogService) { }

  ngOnInit() {
    //this function waits for a message from alert service, it gets   
    //triggered when we call this from any other component  
    this.confirmDialogService.getMessage().subscribe(message => {
      this.message = message;
    });
  }

  public get message(): any {
    return this._message;
  }
  public set message(value: any) {
    this._message = value;
  }
}
