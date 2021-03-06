/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.jmap.api.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.james.core.User;
import org.apache.james.eventsourcing.eventstore.EventStore;
import org.apache.james.jmap.api.filtering.impl.EventSourcingFilteringManagement;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

public interface FilteringManagementContract {

    String BART_SIMPSON_CARTOON = "bart@simpson.cartoon";
    Rule RULE_1 = Rule.of("1");
    Rule RULE_2 = Rule.of("2");
    Rule RULE_3 = Rule.of("3");

    default FilteringManagement instanciateFilteringManagement(EventStore eventStore) {
        return new EventSourcingFilteringManagement(eventStore);
    }

    @Test
    default void listingRulesForUnknownUserShouldReturnEmptyList(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        assertThat(instanciateFilteringManagement(eventStore).listRulesForUser(user)).isEmpty();
    }

    @Test
    default void listingRulesShouldThrowWhenNullUser(EventStore eventStore) {
        User user = null;
        assertThatThrownBy(() -> instanciateFilteringManagement(eventStore).listRulesForUser(user)).isInstanceOf(NullPointerException.class);
    }

    @Test
    default void listingRulesShouldReturnDefinedRules(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        testee.defineRulesForUser(user, ImmutableList.of(RULE_1, RULE_2));
        assertThat(testee.listRulesForUser(user)).containsExactly(RULE_1, RULE_2);
    }

    @Test
    default void listingRulesShouldReturnLastDefinedRules(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        testee.defineRulesForUser(user, ImmutableList.of(RULE_1, RULE_2));
        testee.defineRulesForUser(user, ImmutableList.of(RULE_2, RULE_1));
        assertThat(testee.listRulesForUser(user)).containsExactly(RULE_2, RULE_1);
    }

    @Test
    default void definingRulesShouldThrowWhenDuplicateRules(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        assertThatThrownBy(() -> testee.defineRulesForUser(user, ImmutableList.of(RULE_1, RULE_1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    default void definingRulesShouldThrowWhenNullUser(EventStore eventStore) {
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        assertThatThrownBy(() -> testee.defineRulesForUser(null, ImmutableList.of(RULE_1, RULE_1)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void definingRulesShouldThrowWhenNullRuleList(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        assertThatThrownBy(() -> testee.defineRulesForUser(user, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void definingRulesShouldKeepOrdering(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        testee.defineRulesForUser(user, ImmutableList.of(RULE_3, RULE_2, RULE_1));
        assertThat(testee.listRulesForUser(user)).containsExactly(RULE_3, RULE_2, RULE_1);
    }

    @Test
    default void definingEmptyRuleListShouldRemoveExistingRules(EventStore eventStore) {
        User user = User.fromUsername(BART_SIMPSON_CARTOON);
        FilteringManagement testee = instanciateFilteringManagement(eventStore);
        testee.defineRulesForUser(user, ImmutableList.of(RULE_3, RULE_2, RULE_1));
        testee.defineRulesForUser(user, ImmutableList.of());
        assertThat(testee.listRulesForUser(user)).isEmpty();
    }

}