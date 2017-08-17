package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.features;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.JobView;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction;

/**
 * @author Daniel Beland
 */
public class HasBadges extends BaseFeature<HasBadges.Badges> {
	private ActionFilter filter = new ActionFilter();
	private JobView job;

	public HasBadges() {
	}

	@Override
	public HasBadges of(JobView jobView) {
		this.job = jobView;

		return this;
	}

	@Override
	public Badges asJson() {
		Iterator<GroovyPostbuildAction> badges = Iterables.filter(job.lastBuild().allDetailsOf(GroovyPostbuildAction.class), filter).iterator();

		return badges.hasNext()
				? new Badges(badges)
				: null; // `null` because we don't want to serialise an empty object
	}

	public static class Badges {
		private final List<Badge> badges = newArrayList();

		public Badges(Iterator<GroovyPostbuildAction> badgeActions) {
        	while (badgeActions.hasNext()) {
        		badges.add(new Badge(badgeActions.next()));
        	}
		}
		
		@JsonValue
		public List<Badge> value() {
			return ImmutableList.copyOf(badges);
		}
	}
	
	public static class Badge {
		private final GroovyPostbuildAction badge;
		
		public Badge(GroovyPostbuildAction badge) {
        	this.badge = badge;
		}
		
        @JsonProperty
        public final String text() {
            return badge.getText();
        }

        @JsonProperty
        public final String color() {
            return badge.getColor();
        }

        @JsonProperty
        public final String background() {
            return badge.getBackground();
        }

        @JsonProperty
        public final String border() {
            return badge.getBorder();
        }

        @JsonProperty
        public final String borderColor() {
            return badge.getBorderColor();
        }
	}
	
	private static class ActionFilter implements Predicate<GroovyPostbuildAction> {
		@Override
		public boolean apply(GroovyPostbuildAction action) {
			return action.getIconPath() == null;
		}
	}

}
