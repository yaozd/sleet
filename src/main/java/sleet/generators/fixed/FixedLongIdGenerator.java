/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sleet.generators.fixed;

import java.util.List;
import java.util.Properties;

import sleet.SleetException;
import sleet.generators.GeneratorConfigException;
import sleet.generators.GeneratorSessionException;
import sleet.generators.IdGenerator;
import sleet.id.LongId;
import sleet.id.LongIdType;
import sleet.state.IdState;

public class FixedLongIdGenerator implements IdGenerator<LongIdType> {
  public static final String FIXED_LONG_VALUE_KEY = "fixed.long.value";
  public static final String FIXED_BITS_IN_ID_KEY = "fixed.bits.in.value";

  private LongId value = null;

  @Override
  public void beginIdSession(Properties config) throws SleetException {
    if (this.value != null) {
      throw new GeneratorSessionException("Session was already started.  Stop session by calling endIdSession() then start session by calling beginIdSession()");
    }
    String valueStr = config.getProperty(FIXED_LONG_VALUE_KEY);
    if (valueStr == null) {
      throw new GeneratorConfigException("Missing value for fixed long id generation, must be specified in configuration properties key \"" + FIXED_LONG_VALUE_KEY + "\".");
    }
    long value = -1;
    try {
      value = Long.valueOf(valueStr);
    } catch (NumberFormatException e) {
      throw new GeneratorConfigException("Failed to parse long value from value \"" + valueStr + "\".  The value for configuration properties key \"" + FIXED_LONG_VALUE_KEY + "\" must be a long.");
    }

    String bitsStr = config.getProperty(FIXED_BITS_IN_ID_KEY);
    if (bitsStr == null) {
      throw new GeneratorConfigException("Missing number of bits for the fixed value, must be specified in configuration properties key \"" + FIXED_BITS_IN_ID_KEY + "\".");
    }
    int bits = -1;
    try {
      bits = Integer.valueOf(bitsStr);
    } catch (NumberFormatException e) {
      throw new GeneratorConfigException("Failed to parse number of bits from value \"" + bitsStr + "\".  The value for configuration properties key \"" + FIXED_BITS_IN_ID_KEY + "\" must be a long.");
    }

    if ((1L << bits) - 1L < value) {
      throw new GeneratorConfigException("Specified value of " + value + " exceeds the capacity of the number of bits specified (" + bits + ").");
    }

    this.value = new LongId(value, null, bits);
  }

  @Override
  public void checkSessionValidity() throws SleetException {
    validateSessionStarted();
  }

  @Override
  public void endIdSession() throws SleetException {
    validateSessionStarted();
    this.value = null;
  }

  @Override
  public LongIdType getId(List<IdState<?, ?>> states) throws SleetException {
    validateSessionStarted();
    return this.value;
  }

  private void validateSessionStarted() throws GeneratorSessionException {
    if (this.value == null) {
      throw new GeneratorSessionException("Session was not started.  Start session by calling beginIdSession()");
    }
  }
}
