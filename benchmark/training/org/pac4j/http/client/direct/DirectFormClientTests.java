package org.pac4j.http.client.direct;


import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.pac4j.core.context.MockWebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;


/**
 * This class tests the {@link DirectFormClient} class.
 *
 * @author Jerome Leleu
 * @since 1.8.6
 */
public final class DirectFormClientTests implements TestsConstants {
    @Test
    public void testMissingUsernamePasswordAuthenticator() {
        final DirectFormClient formClient = new DirectFormClient(null);
        TestsHelper.expectException(() -> formClient.getCredentials(MockWebContext.create()), TechnicalException.class, "authenticator cannot be null");
    }

    @Test
    public void testMissingProfileCreator() {
        final DirectFormClient formClient = new DirectFormClient(new SimpleTestUsernamePasswordAuthenticator(), null);
        TestsHelper.expectException(() -> formClient.getUserProfile(new <USERNAME, PASSWORD>UsernamePasswordCredentials(), MockWebContext.create()), TechnicalException.class, "profileCreator cannot be null");
    }

    @Test
    public void testHasDefaultProfileCreator() {
        final DirectFormClient formClient = new DirectFormClient(new org.pac4j.core.credentials.authenticator.LocalCachingAuthenticator(new SimpleTestUsernamePasswordAuthenticator(), 10, 10, TimeUnit.DAYS));
        formClient.init();
    }

    @Test
    public void testGetCredentialsMissingUsername() {
        final DirectFormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        Assert.assertFalse(formClient.getCredentials(context.addRequestParameter(formClient.getUsernameParameter(), USERNAME)).isPresent());
    }

    @Test
    public void testGetCredentialsMissingPassword() {
        final DirectFormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        Assert.assertFalse(formClient.getCredentials(context.addRequestParameter(formClient.getPasswordParameter(), PASSWORD)).isPresent());
    }

    @Test
    public void testGetBadCredentials() {
        final DirectFormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        Assert.assertFalse(formClient.getCredentials(context.addRequestParameter(formClient.getUsernameParameter(), USERNAME).addRequestParameter(formClient.getPasswordParameter(), PASSWORD)).isPresent());
    }

    @Test
    public void testGetGoodCredentials() {
        final DirectFormClient formClient = getFormClient();
        final UsernamePasswordCredentials credentials = formClient.getCredentials(MockWebContext.create().addRequestParameter(formClient.getUsernameParameter(), USERNAME).addRequestParameter(formClient.getPasswordParameter(), USERNAME)).get();
        Assert.assertEquals(USERNAME, credentials.getUsername());
        Assert.assertEquals(USERNAME, credentials.getPassword());
    }

    @Test
    public void testGetUserProfile() {
        final DirectFormClient formClient = getFormClient();
        formClient.setProfileCreator(( credentials, context) -> {
            String username = credentials.getUsername();
            final CommonProfile profile = new CommonProfile();
            profile.setId(username);
            profile.addAttribute(Pac4jConstants.USERNAME, username);
            return Optional.of(profile);
        });
        final MockWebContext context = MockWebContext.create();
        final CommonProfile profile = ((CommonProfile) (formClient.getUserProfile(new UsernamePasswordCredentials(USERNAME, USERNAME), context).get()));
        Assert.assertEquals(USERNAME, profile.getId());
        Assert.assertEquals((((CommonProfile.class.getName()) + (CommonProfile.SEPARATOR)) + (USERNAME)), profile.getTypedId());
        Assert.assertTrue(ProfileHelper.isTypedIdOf(profile.getTypedId(), CommonProfile.class));
        Assert.assertEquals(USERNAME, profile.getUsername());
        Assert.assertEquals(1, profile.getAttributes().size());
    }
}
