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
/**
 * \@author Sandeep.Mantha
 * \@whatItDoes 
 * 
 * \@howToUse 
 * 
 */
import { Converter } from './object.conversion';
import { Serializable } from './serializable';

export class Message implements Serializable<Message, string> {
    type: string;
    text: string;
    context: string;
    messageArray: any[] = [];
    life: number;
    
    
    deserialize( inJson ) {

        let obj: Message = this;
        obj = Converter.convert(inJson,obj);

        if(this.context !== undefined){
            obj = Message.createMessage(this.type, this.context, this.text, this.life);
        }

        return obj;
    }

    /**
     * \@author Andrew.Jo
     * \@whatItDoes 
     *      Creates a customizable instance of message class that can show up in the top right corner of the screen
     * \@howToUse 
     *      Call this createMessage method by providing it with 4 variables
     */
    public static createMessage( type, context, detail, life ): Message {
        let severity: string, summary: string;
        
        let message = new Message();
        message.context = context;
        message.type = type;
        message.life = life;
        
        switch (type) {               

            case 'SUCCESS': 
                severity = 'success'; summary = 'Success Message';
                if(life != undefined)
                    message.life = life;
                else    
                    message.life = 3000;
                break;
            case 'DANGER': 
                severity = 'error'; summary = 'Error Message';
                if(life != undefined)
                    message.life = life;
                else    
                    message.life = 10000;
                break; 
            case 'WARNING': 
                severity = 'warn'; summary = 'Warn Message';
                if(life != undefined)
                    message.life = life;
                else    
                    message.life = 5000;
                break;
            case 'INFO': 
                severity = 'info'; summary = 'Info Message';
                if(life != undefined)
                    message.life = life;
                else    
                    message.life = 3000;
                break;   
        }
        message.messageArray = [];
        if (message.context === 'INLINE') {
            message.messageArray.push({severity: severity,  summary: "",  detail: detail});
        }
        else {
            message.messageArray.push({severity: severity, summary: summary, detail: detail});
        }
        return message;
    }
}
