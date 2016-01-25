package com.qmatic.common.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BranchGeoCalculator {

    private static final Logger log = LoggerFactory.getLogger(BranchGeoCalculator.class);

    public List<Branch> sortBranchesByDistance(List<Branch> branches, Double longitude, Double latitude,
                                               Integer radius, Integer maxNrOfBranches) {
        DistanceCalculator calc = new DistanceCalculator();
        // First put branches in a treemap to get them sorted by distance
        Map<Double, Branch> distMap = new TreeMap<>();

        for (Iterator<Branch> iter = branches.iterator(); iter.hasNext();) {
            Branch branch = iter.next();
            if (radius != 0 && !calc.inRange(branch.getLongitude(), branch.getLatitude(),
                longitude, latitude, radius)) {
                // Remove branches out of range from current position.
                iter.remove();
            } else {
                Double distance = calc.getDistance(branch.getLongitude(), branch.getLatitude(), longitude, latitude);
                log.debug("Distance for branch " + branch.getId() + " : " + distance.toString());
                distMap.put(distance, branch);
                determineOpenCloseFlags(branch);
            }
        }

        // If maxNrOfBranches is zero don't limit on the max number of branches
        if(maxNrOfBranches == 0) {
            maxNrOfBranches = Integer.MAX_VALUE;
        }

        // We're only interested in the N closest branches
        Integer numberOfBranches = Math.min(distMap.size(), maxNrOfBranches);
        List<Branch> filteredBranches = new ArrayList<>(distMap.values()).subList(0, numberOfBranches);

        return filteredBranches;
    }

    public List<Branch> cullBranchesOutOfRange(List<Branch> branches, Double longitude, Double latitude, Integer radius) {
        List<Branch> filteredBranches = new ArrayList<>(branches);
        DistanceCalculator calc = new DistanceCalculator();

        for (Iterator<Branch> iter = filteredBranches.iterator(); iter.hasNext();) {
            Branch branch = iter.next();
            if (radius != 0 && !calc.inRange(branch.getLongitude(), branch.getLatitude(),
                longitude, latitude, radius)) {
                // Remove branches out of range from current position.
                iter.remove();
            } else {
                determineOpenCloseFlags(branch);
            }
        }
        return filteredBranches;
    }


    public void determineOpenCloseFlags(Branch branch) {
        Calendar branchCurrentTime;
        if (branch.getTimeZone() != null) {
            TimeZone branchTZ = TimeZone.getTimeZone(branch.getTimeZone());
            branchCurrentTime = Calendar.getInstance(branchTZ);
        } else {
            log.debug("Branch " + branch.getName() + " (id:" + branch.getId() + ") does not have a timezone. Creating default system timezone instead to calculate opening/closing hours");
            branchCurrentTime = Calendar.getInstance();
        }

        if (branchCurrentTime == null || branch.getOpenTime() == null || branch.getOpenTime().isEmpty() || branch.getCloseTime() == null || branch.getCloseTime().isEmpty()) {
            branch.setBranchOpen(true);
            branch.setQueuePassesClosingTime(false);
            log.warn("Open/Close time not fully set or could not get current time for branchId: {}", branch.getId());
            return;
        }

        log.debug("Current time from branch {} : {} (timezone: {})", new Object[] {branch.getName(), branchCurrentTime, branch.getTimeZone()});

        int openInMinutes = (Integer.parseInt(branch.getOpenTime().substring(0, 2)) * 60) + Integer.parseInt(branch.getOpenTime().substring(3, 5));
        int closeInMinutes = (Integer.parseInt(branch.getCloseTime().substring(0, 2)) * 60) + Integer.parseInt(branch.getCloseTime().substring(3, 5));
        int nowInMinutes = (branchCurrentTime.get(Calendar.HOUR_OF_DAY) * 60) + branchCurrentTime.get(Calendar.MINUTE);

        if ((nowInMinutes >= openInMinutes && nowInMinutes < closeInMinutes)) {
            log.debug("Branch " + branch.getName() + " is open");
            branch.setBranchOpen(true);
            nowInMinutes += branch.getEstimatedWaitTime();
            if (nowInMinutes >= closeInMinutes) {
                branch.setQueuePassesClosingTime(true);
            } else {
                branch.setQueuePassesClosingTime(false);
            }
        } else {
            log.debug("Branch " + branch.getName() + " is closed");
            branch.setBranchOpen(false);
        }
    }

}
