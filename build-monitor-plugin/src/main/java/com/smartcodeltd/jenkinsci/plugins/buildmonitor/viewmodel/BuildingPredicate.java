package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel;

import java.util.function.Predicate;
import hudson.model.Run;

import javax.annotation.Nullable;

public final class BuildingPredicate implements Predicate<Run<?, ?>> {

	public static final BuildingPredicate INSTANCE = new BuildingPredicate();
	
	private BuildingPredicate() {
	}
	
	@Override
	public boolean test(@Nullable Run<?, ?> run) {
		if (run == null) {
			throw new RuntimeException("Run was null");
		}
		return run.isBuilding();
	}

}
