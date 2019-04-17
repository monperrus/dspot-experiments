/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.alarmcallbacks;


import EmailRecipients.Factory;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.graylog2.alerts.AlertSender;
import org.graylog2.configuration.EmailConfiguration;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.users.UserService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;


public class EmailAlarmCallbackTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private AlertSender alertSender = Mockito.mock(AlertSender.class);

    private NotificationService notificationService = Mockito.mock(NotificationService.class);

    private NodeId nodeId = Mockito.mock(NodeId.class);

    private Factory emailRecipientsFactory = Mockito.mock(Factory.class);

    private UserService userService = Mockito.mock(UserService.class);

    private EmailConfiguration emailConfiguration = Mockito.mock(EmailConfiguration.class);

    private Configuration graylogConfig = Mockito.mock(Configuration.class);

    private EmailAlarmCallback alarmCallback;

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration() throws Exception {
        final Map<String, Object> configMap = ImmutableMap.of("sender", "graylog@example.org", "subject", "Graylog alert", "body", "foobar", "user_receivers", Collections.emptyList(), "email_receivers", Collections.emptyList());
        final Configuration configuration = new Configuration(configMap);
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test
    public void checkConfigurationSucceedsWithFallbackSender() throws Exception {
        final Map<String, Object> configMap = ImmutableMap.of("subject", "Graylog alert", "body", "foobar", "user_receivers", Collections.emptyList(), "email_receivers", Collections.emptyList());
        final Configuration configuration = new Configuration(configMap);
        Mockito.when(emailConfiguration.getFromEmail()).thenReturn("default@sender.org");
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test
    public void checkConfigurationFailsWithoutSender() throws Exception {
        final Map<String, Object> configMap = ImmutableMap.of("subject", "Graylog alert", "body", "foobar", "user_receivers", Collections.emptyList(), "email_receivers", Collections.emptyList());
        final Configuration configuration = new Configuration(configMap);
        alarmCallback.initialize(configuration);
        Mockito.when(emailConfiguration.getFromEmail()).thenReturn("");
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Sender or subject are missing or invalid.");
        alarmCallback.checkConfiguration();
    }

    @Test
    public void checkConfigurationFailsWithoutSubject() throws Exception {
        final Map<String, Object> configMap = ImmutableMap.of("sender", "graylog@example.org", "body", "foobar", "user_receivers", Collections.emptyList(), "email_receivers", Collections.emptyList());
        final Configuration configuration = new Configuration(configMap);
        alarmCallback.initialize(configuration);
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Sender or subject are missing or invalid.");
        alarmCallback.checkConfiguration();
    }

    @Test
    public void getEnrichedRequestedConfigurationReturnsUsersListIncludingAdminUser() throws Exception {
        final String userName = "admin";
        Mockito.when(graylogConfig.getRootUsername()).thenReturn(userName);
        final ConfigurationRequest configuration = alarmCallback.getEnrichedRequestedConfiguration();
        assertThat(configuration.containsField("user_receivers")).isTrue();
        final Map<String, String> users = configuration.getField("user_receivers").getAdditionalInformation().get("values");
        assertThat(users).containsEntry(userName, userName);
    }
}
