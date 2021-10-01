/**
 * Copyright (c) 2021 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.zwave.commands.securityv1;

import com.parrotha.zwave.commands.doorlockv1.DoorLockOperationSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecurityMessageEncapsulationTest {
    @Test
    public void testEncapsulate() {
        DoorLockOperationSet cmd = new DoorLockOperationSet();
        cmd.setDoorLockMode(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_SECURED);
        assertEquals("9881006201FF", new SecurityMessageEncapsulation().encapsulate(cmd).format());
    }
}