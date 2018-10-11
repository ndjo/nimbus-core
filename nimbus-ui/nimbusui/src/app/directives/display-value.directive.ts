/**
 * @license
 * Copyright 2016-2018 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';
import { Directive, ElementRef, Renderer2, Input, SimpleChanges } from '@angular/core';
import { ParamConfig } from '../shared/param-config';
import { Param } from "../shared/param-state";
/**
 * \@author Dinakar.Meda
 * \@whatItDoes 
 * 
 * \@howToUse 
 * 
 */
@Directive({
  selector: '[nmDisplayValue]'
})
export class DisplayValueDirective {
    @Input('nmDisplayValue') displayValue: any;
    @Input('config') config: ParamConfig;
    @Input('path') path: Param;


    static placeholder: string = 'placeholder';
    private static readonly DEFAULT_VALUE_STYLES_PATH = '/';

    constructor(private el: ElementRef, private renderer: Renderer2) {
    }

    /*
        Initialize the component with the styles. The field name and value are applied as style classes 
        if the applyValueStyles attribute is set to true.
    */
    ngOnInit() {

        this.apply();

    }

    /*
        Handle value changes 
    */
    ngOnChanges(changes: SimpleChanges) {

        this.apply(changes);

    }

    /**
     * 
     * @param changes 
     */
    private apply(changes?: SimpleChanges): void {

        this.config;

        if (this.config && this.config.uiStyles.attributes.applyValueStyles) {
            this.renderer.addClass(this.el.nativeElement, this.config.code); // Field Name
            let finalDisplayValue = changes ? changes.displayValue.currentValue : this.displayValue;
            // If valueStylesPath is anything other than default, then retrieve the value 
            // of the param identified by valueStylesPath and prefer it.
            let valueStylesPath = this.config.uiStyles.attributes.valueStylesPath;
            // "/../nickname"
            
            if (DisplayValueDirective.DEFAULT_VALUE_STYLES_PATH !== valueStylesPath) {
                
                // TODO Retrieve the value of the param identified by valueStylesPath.
                // window.location.hash.split('#')[1]
                // document.getElementsByClassName("nickname")[0].innerText
                console.log(this.path);
                let retrievedValue = "Andy";
                 // If found, Set the retrieved value into finalDisplayValue
                if (retrievedValue) {
                    finalDisplayValue = retrievedValue;
                }
            }

            // Remove the previous value styles (if applicable)
            if (changes && changes.displayValue.previousValue === undefined) {
                this.renderer.removeClass(this.el.nativeElement, DisplayValueDirective.placeholder);
            } else if (changes){
                this.renderer.removeClass(this.el.nativeElement, this.getValue(changes.displayValue.previousValue));
            }
            // Add the value styles
            if (finalDisplayValue && (!this.isEmpty(finalDisplayValue))) {
                this.renderer.addClass(this.el.nativeElement, this.getValue(finalDisplayValue)); // Field Value
            } else {
                this.renderer.addClass(this.el.nativeElement, DisplayValueDirective.placeholder); // placeholder Value
            }
        }

    }

     /*
        Remove spaces from value since style class cannot take spaces
     */
    getValue(val: any): any {
        var re = / /gi;
        if (val instanceof String) {
            return val.replace(re, "");
        } else {
            return val;
        }
    }
     /**
     * Determine whether or not the provided string is empty. The provided
     * string is considered empty if it is undefined or contains only whitespace.
     * @param str the string to check
     */
    private isEmpty(str: string): boolean {
        return !str || str.trim() === '';
    }
}