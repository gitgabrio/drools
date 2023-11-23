/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.internal.command.RegistryContext;

@XmlRootElement(name="get-variable-command")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetVariableCommand implements ExecutableCommand<Object> {
   
    @XmlElement
    @XmlSchemaType(name="string")
    private String identifier;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private String contextName;
    
    public GetVariableCommand() {
        // no-arg constructor neccessary for serialization
    }    

    public GetVariableCommand(String identifier) {
        this.identifier = identifier;
    }    

    public GetVariableCommand(String identifier,
                              String contextName) {
        this.identifier = identifier;
        this.contextName = contextName;
    }

    public Object execute(Context ctx) {        
        Context targetCtx;
        if ( this.contextName == null ) {
            targetCtx = ctx;
        } else {
            targetCtx = ( (RegistryContext) ctx ).getContextManager().getContext( this.contextName );
        }
        
        return targetCtx.get( identifier);        
    }

}
