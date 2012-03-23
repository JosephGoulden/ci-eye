package org.netmelody.cieye.spies.teamcity.test;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.netmelody.cieye.core.observation.CodeBook;
import org.netmelody.cieye.core.observation.CommunicationNetwork;
import org.netmelody.cieye.spies.StubContact;
import org.netmelody.cieye.spies.teamcity.TeamCityCommunicator;
import org.netmelody.cieye.spies.teamcity.jsondomain.BuildDetail;
import org.netmelody.cieye.spies.teamcity.jsondomain.Change;
import org.netmelody.cieye.spies.teamcity.jsondomain.ChangesHref;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class TeamCityCommunicatorTest {

    private final Mockery context = new Mockery();
    
    private final CommunicationNetwork network = context.mock(CommunicationNetwork.class);
    private final StubContact contact = new StubContact();
    
    private TeamCityCommunicator communicator;
    
    @Before
    public void setup() {
        context.checking(new Expectations() {{
            allowing(network).makeContact(with(any(CodeBook.class))); will(returnValue(contact));
        }});
        communicator = new TeamCityCommunicator(network, "http://foo");
    }
    
    @Test public void
    requestsSingularBuildChangesForTeamCitySixApi() {
        final BuildDetail buildDetail = buildDetail(1);
        contact.respondingWith("http://foo" + buildDetail.changes.href, contentFrom("tc_6.5.5_changes_1.json").replace("@", ""));
        
        final List<Change> changes = communicator.changesOf(buildDetail);
        assertThat(changes, is(Matchers.<Change>iterableWithSize(1)));
        assertThat(changes.get(0).id, is("48834"));
    }

    @Test public void
    requestsMultipleBuildChangesForTeamCitySixApi() {
        final BuildDetail buildDetail = buildDetail(2);
        contact.respondingWith("http://foo" + buildDetail.changes.href, contentFrom("tc_6.5.5_changes_2.json").replace("@", ""));

        final List<Change> changes = communicator.changesOf(buildDetail);
        assertThat(changes, is(Matchers.<Change>iterableWithSize(2)));
        assertThat(changes.get(0).id, is("47951"));
        assertThat(changes.get(1).id, is("47949"));
    }

    @Test public void
    requestsSingularBuildChangesForTeamCitySevenApi() {
        final BuildDetail buildDetail = buildDetail(1);
        contact.respondingWith("http://foo" + buildDetail.changes.href, contentFrom("tc_7.0.0_changes_1.json"));
        
        final List<Change> changes = communicator.changesOf(buildDetail);
        assertThat(changes, is(Matchers.<Change>iterableWithSize(1)));
        assertThat(changes.get(0).id, is("62889"));
    }

    @Test public void
    requestsMultipleBuildChangesForTeamCitySevenApi() {
        final BuildDetail buildDetail = buildDetail(2);
        contact.respondingWith("http://foo" + buildDetail.changes.href, contentFrom("tc_7.0.0_changes_2.json"));
        
        final List<Change> changes = communicator.changesOf(buildDetail);
        assertThat(changes, is(Matchers.<Change>iterableWithSize(2)));
        assertThat(changes.get(0).id, is("62855"));
        assertThat(changes.get(1).id, is("62854"));
    }

    private BuildDetail buildDetail(int size) {
        final BuildDetail buildDetail = new BuildDetail();
        buildDetail.changes = new ChangesHref();
        buildDetail.changes.count = size;
        buildDetail.changes.href = "/app/rest/changes/id:12345";
        return buildDetail;
    }

    private String contentFrom(String resourceName) {
        try {
            return IOUtils.toString(TeamCityCommunicatorTest.class.getResourceAsStream(resourceName));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}