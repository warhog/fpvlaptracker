import { Directive, Input, ElementRef, AfterContentInit } from '@angular/core';

@Directive({
  selector: '[appAutofocus]'
})
export class AutofocusDirective implements AfterContentInit {

  constructor(private el: ElementRef) { }

  @Input()
  public appAutoFocus: boolean;

  public ngAfterContentInit() {
    setTimeout(() => {
      this.el.nativeElement.focus();
    }, 500);
  }

}
