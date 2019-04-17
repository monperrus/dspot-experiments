/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.bindy.fixed.marshall.simple;


import BindyType.Fixed;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.dataformat.bindy.annotation.FixedLengthRecord;
import org.apache.camel.model.dataformat.BindyDataFormat;
import org.apache.camel.spring.boot.TypeConversionConfiguration;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BindySimpleFixedLengthObjectMarshallTest.Configuration.class, TypeConversionConfiguration.class }, loader = CamelSpringDelegatingTestContextLoader.class)
public class BindySimpleFixedLengthObjectMarshallTest extends AbstractJUnit4SpringContextTests {
    private static final String URI_MOCK_RESULT = "mock:result";

    private static final String URI_MOCK_ERROR = "mock:error";

    private static final String URI_DIRECT_START = "direct:start";

    private String expected;

    @Produce(uri = BindySimpleFixedLengthObjectMarshallTest.URI_DIRECT_START)
    private ProducerTemplate template;

    @EndpointInject(uri = BindySimpleFixedLengthObjectMarshallTest.URI_MOCK_RESULT)
    private MockEndpoint result;

    @EndpointInject(uri = BindySimpleFixedLengthObjectMarshallTest.URI_MOCK_ERROR)
    private MockEndpoint error;

    public static class Configuration extends SingleRouteCamelConfiguration {
        @Bean
        @Override
        public RouteBuilder route() {
            return new RouteBuilder() {
                public void configure() {
                    // default should errors go to mock:error
                    errorHandler(deadLetterChannel(BindySimpleFixedLengthObjectMarshallTest.URI_MOCK_ERROR).redeliveryDelay(0));
                    onException(Exception.class).maximumRedeliveries(0).handled(true);
                    BindyDataFormat bindy = new BindyDataFormat();
                    bindy.setLocale("en");
                    bindy.setClassType(BindySimpleFixedLengthObjectMarshallTest.Order.class);
                    bindy.setType(Fixed);
                    from(BindySimpleFixedLengthObjectMarshallTest.URI_DIRECT_START).marshal(bindy).to(BindySimpleFixedLengthObjectMarshallTest.URI_MOCK_RESULT);
                }
            };
        }
    }

    @Test
    @DirtiesContext
    public void testMarshallObject() throws Exception {
        expected = "10A9  PaulineM    ISINXD12345678BUYShare000002500.45USD01-08-2009\r\n";
        result.expectedBodiesReceived(expected);
        error.expectedMessageCount(0);
        template.sendBody(generateModel("Pauline"));
        error.assertIsSatisfied();
        result.assertIsSatisfied();
    }

    @Test
    @DirtiesContext
    public void testMarshallList() throws Exception {
        expected = "10A9  PaulineM    ISINXD12345678BUYShare000002500.45USD01-08-2009\r\n" + "10A9  MarcoolM    ISINXD12345678BUYShare000002500.45USD01-08-2009\r\n";
        result.expectedBodiesReceived(expected);
        error.expectedMessageCount(0);
        List<BindySimpleFixedLengthObjectMarshallTest.Order> list = new ArrayList<>();
        list.add(generateModel("Pauline"));
        list.add(generateModel("Marcool"));
        template.sendBody(list);
        error.assertIsSatisfied();
        result.assertIsSatisfied();
    }

    @FixedLengthRecord(length = 65, paddingChar = ' ')
    public static class Order {
        @DataField(pos = 1, length = 2)
        private int orderNr;

        @DataField(pos = 3, length = 2)
        private String clientNr;

        @DataField(pos = 5, length = 9)
        private String firstName;

        @DataField(pos = 14, length = 5, align = "L")
        private String lastName;

        @DataField(pos = 19, length = 4)
        private String instrumentCode;

        @DataField(pos = 23, length = 10)
        private String instrumentNumber;

        @DataField(pos = 33, length = 3)
        private String orderType;

        @DataField(pos = 36, length = 5)
        private String instrumentType;

        @DataField(pos = 41, precision = 2, length = 12, paddingChar = '0')
        private BigDecimal amount;

        @DataField(pos = 53, length = 3)
        private String currency;

        @DataField(pos = 56, length = 10, pattern = "dd-MM-yyyy")
        private Date orderDate;

        public int getOrderNr() {
            return orderNr;
        }

        public void setOrderNr(int orderNr) {
            this.orderNr = orderNr;
        }

        public String getClientNr() {
            return clientNr;
        }

        public void setClientNr(String clientNr) {
            this.clientNr = clientNr;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getInstrumentCode() {
            return instrumentCode;
        }

        public void setInstrumentCode(String instrumentCode) {
            this.instrumentCode = instrumentCode;
        }

        public String getInstrumentNumber() {
            return instrumentNumber;
        }

        public void setInstrumentNumber(String instrumentNumber) {
            this.instrumentNumber = instrumentNumber;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getInstrumentType() {
            return instrumentType;
        }

        public void setInstrumentType(String instrumentType) {
            this.instrumentType = instrumentType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public Date getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(Date orderDate) {
            this.orderDate = orderDate;
        }

        @Override
        public String toString() {
            return (((((((((((((((((((((("Model : " + (BindySimpleFixedLengthObjectMarshallTest.Order.class.getName())) + " : ") + (this.orderNr)) + ", ") + (this.orderType)) + ", ") + (String.valueOf(this.amount))) + ", ") + (this.instrumentCode)) + ", ") + (this.instrumentNumber)) + ", ") + (this.instrumentType)) + ", ") + (this.currency)) + ", ") + (this.clientNr)) + ", ") + (this.firstName)) + ", ") + (this.lastName)) + ", ") + (String.valueOf(this.orderDate));
        }
    }
}
