package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim;

import hudson.plugins.claim.ClaimBuildAction;

import org.junit.Test;

import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.User;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClaimedTest {
    private final static String AUTHOR = "Adam";
    private final static String REASON = "I broke it, sorry, fixing now.";
    private final static String EXPECTED_STRING_REPRESENTATION = String.format("Claimed by \"%s\": \"%s\"", AUTHOR, REASON);

    @Test
    public void should_be_a_simple_proxy_to_the_claim_build_action() throws Exception {
        Claim claim = new Claimed(claimBuildAction(new User(AUTHOR), REASON));

        assertThat(claim.wasMade(),  is(true));
        assertThat(claim.author(),   equalTo(AUTHOR));
        assertThat(claim.reason(),   equalTo(REASON));

        assertThat(claim.toString(), is(EXPECTED_STRING_REPRESENTATION));
    }

    public static ClaimBuildAction claimBuildAction(User author, String reason) {
        ClaimBuildAction action = mock(ClaimBuildAction.class);
        when(action.isClaimed()).thenReturn(true);
        when(action.getClaimedByName()).thenReturn(author.getName());
        when(action.getReason()).thenReturn(reason);

        return action;
    }
}