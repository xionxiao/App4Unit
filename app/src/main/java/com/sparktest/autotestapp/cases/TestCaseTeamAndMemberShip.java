package com.sparktest.autotestapp.cases;

import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.team.Team;
import com.ciscospark.androidsdk.team.TeamMembership;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.sparktest.autotestapp.framework.Verify.verifyTrue;

/**
 * Created by qimdeng on 11/22/17.
 */

public class TestCaseTeamAndMemberShip extends TestSuite {
    public TestCaseTeamAndMemberShip() {
        this.add(TestCaseTeamAndMemberShip.TeamCreator.class);
    }

    @Description("Team & MemberShip")
    public static class TeamCreator {
        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;

        TestActor actor;
        Team team;
        List<TeamMembership> teamMemberShipList = new ArrayList<>();

        @Test
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey1);
            actor.login(this::onRegistered);
        }

        private void onRegistered(Result result) {
            Ln.d("onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getSpark().teams().create("TestTeam", new CompletionHandler<Team>() {
                    @Override
                    public void onComplete(Result<Team> result) {
                        Ln.d("onTeamCreated result: " + result.isSuccessful());
                        if (result.isSuccessful()) {
                            team = result.getData();
                            Ln.d("Team name: " + team.getName());
                            Verify.verifyTrue(team.getName().equals("TestTeam"));
                            addMembershipToTeam();
                        } else {
                            Verify.verifyTrue(false);
                        }
                    }
                });

            } else {
                Verify.verifyTrue(false);
            }
        }

        private void addMembershipToTeam() {
            Ln.d("addMembershipToTeam");
            actor.getSpark().teamMembershipClient().create(team.getId(), TestActor.jwtUserID2,
                    null, false, this::onTeamMembershipCreated);
            actor.getSpark().teamMembershipClient().create(team.getId(), TestActor.jwtUserID3,
                    null, false, this::onTeamMembershipCreated);
        }

        private void onTeamMembershipCreated(Result<TeamMembership> result) {
            Ln.d("onMembershipCreated: " + result.isSuccessful());
            if (result.isSuccessful()) {
                TeamMembership teamMembership = result.getData();
                Ln.d("teamMembership name: " + teamMembership.getPersonEmail());
                teamMemberShipList.add(teamMembership);
                if (teamMemberShipList.size() == 2)
                    listTeamMemberShips();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void listTeamMemberShips() {
            Ln.d("listTeamMemberShips");
            actor.getSpark().teamMembershipClient().list(team.getId(), 0, new CompletionHandler<List<TeamMembership>>(){
                @Override
                public void onComplete(Result<List<TeamMembership>> result) {
                    Ln.d("onTeamMemberShipListed: " + result.isSuccessful() + "  list size: " + result.getData().size());
                    if (result.isSuccessful() && result.getData().size() >= 3) {
                        deleteTeamMembership();
                    } else {
                        Verify.verifyTrue(false);
                    }
                }
            });
        }

        private void deleteTeamMembership() {
            Ln.d("deleteTeamMembership");
            actor.getSpark().teamMembershipClient().delete(teamMemberShipList.get(0).getId(), result -> {
                Ln.d("deleteTeamMembership: " + result.isSuccessful());
                if (result.isSuccessful()) {
                    updateTeamMembership();
                } else {
                    Verify.verifyTrue(false);
                }
            });
        }

        private void updateTeamMembership() {
            Ln.d("updateTeamMembership");
            actor.getSpark().teamMembershipClient().update(teamMemberShipList.get(1).getId(), true,
                new CompletionHandler<TeamMembership>() {
                    @Override
                    public void onComplete(Result<TeamMembership> result) {
                        Ln.d("onTeamMembershipUpdated: " + result.isSuccessful() + "  moderator: " + result.getData().isModerator());
                        if (result.isSuccessful() && result.getData().isModerator()) {
                            getTeamDetail();
                        } else {
                            Verify.verifyTrue(false);
                        }
                    }
                });
        }

        private void getTeamDetail(){
            Ln.d("getTeamDetail");
            actor.getSpark().teams().get(team.getId(), new CompletionHandler<Team>() {
                @Override
                public void onComplete(Result<Team> result) {
                    Ln.d("getTeamDetail result: " + result.isSuccessful() + "  name: " + result.getData().getName());
                    if (result.isSuccessful() && result.getData().getName().equals("TestTeam")) {
                        updateTeam();
                    } else {
                        Verify.verifyTrue(false);
                    }
                }
            });
        }

        private void updateTeam(){
            Ln.d("updateTeam");
            actor.getSpark().teams().update(team.getId(),"TestUpdatedTeam",  new CompletionHandler<Team>() {
                @Override
                public void onComplete(Result<Team> result) {
                    Ln.d("updateTeam result: " + result.isSuccessful() + "  name: " + result.getData().getName());
                    if (result.isSuccessful() && result.getData().getName().equals("TestUpdatedTeam")) {
                        deleteTeam();
                    } else {
                        Verify.verifyTrue(false);
                    }
                }
            });
        }

        private void deleteTeam(){
            Ln.d("deleteTeam");
            actor.getSpark().teams().delete(team.getId(), result -> {
                Ln.d("deleteTeam result: " + result.isSuccessful());
                Verify.verifyTrue(result.isSuccessful());
                actor.logout();
            });
        }
    }
}
