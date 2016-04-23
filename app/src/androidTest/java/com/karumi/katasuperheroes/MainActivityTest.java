/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.karumi.katasuperheroes.matchers.RecyclerViewItemsCountMatcher.recyclerViewHasItemCount;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class) @LargeTest public class MainActivityTest {

  @Rule public DaggerMockRule<MainComponent> daggerRule =
      new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
          new DaggerMockRule.ComponentSetter<MainComponent>() {
            @Override public void setComponent(MainComponent component) {
              SuperHeroesApplication app =
                  (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                      .getTargetContext()
                      .getApplicationContext();
              app.setComponent(component);
            }
          });

  @Rule public IntentsTestRule<MainActivity> activityRule =
      new IntentsTestRule<>(MainActivity.class, true, false);

  @Mock SuperHeroesRepository repository;

  @Test public void showsEmptyCaseIfThereAreNoSuperHeroes() {
    givenThereAreNoSuperHeroes();

    startActivity();

    onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
  }

  @Test
  public void doesNotShowEmptyCaseIfThereAreSuperHeroes() throws Exception {
    givenThereAreSomeSuperHeroes(10, false);

    startActivity();

    onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())));
  }

  @Test
  public void testShowsTheNumberOfSuperHeroes() throws Exception {
    int numberOfSuperHeroes = 10;
    givenThereAreSomeSuperHeroes(numberOfSuperHeroes, true);

    startActivity();

    onView(withId(R.id.recycler_view)).check(matches(recyclerViewHasItemCount(numberOfSuperHeroes)));
  }

  @Test
  public void showsSuperHeroesName() throws Exception {
    int numberOfSuperHeroes = 1000;
    List<SuperHero> superHeros = givenThereAreSomeSuperHeroes(numberOfSuperHeroes, false);

    startActivity();

    for (int i = 0; i < numberOfSuperHeroes; i++) {
      onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(i));
      onView(withText("Super Hero - " + i)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void showsAvengersBadgeIfTheSuperHeroIsPartOfTheAvengersTeam() throws Exception {
    int numberOfSuperHeroes = 1000;
    List<SuperHero> superHeros = givenThereAreSomeSuperHeroes(numberOfSuperHeroes, true);

    startActivity();

    RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view))
            .withItems(superHeros)
            .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
              @Override public void check(SuperHero superHero, View view, NoMatchingViewException e) {
                matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge),
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))).check(view, e);
              }
            });
  }

  @Test
  public void showsDetailViewWhenASuperHeroIsClicked() throws Exception {
    SuperHero superHero = givenThereAreSomeSuperHeroes(1, false).get(0);

    startActivity();

    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    intended(hasComponent(hasClassName(SuperHeroDetailActivity.class.getCanonicalName())));
    intended(hasExtra(SuperHeroDetailActivity.SUPER_HERO_NAME_KEY, superHero.getName()));
  }

  private List<SuperHero> givenThereAreSomeSuperHeroes(int numberOfSuperHeroes, boolean avengers) {
    List<SuperHero> superHeores = new ArrayList<>(numberOfSuperHeroes);
    for (int i = 0; i < numberOfSuperHeroes; i++) {
      String name = "Super Hero - " + i;
      String photo = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg";
      boolean isAvenger = avengers;
      String description = "This is the Super Hero " + i;
      SuperHero superHero = new SuperHero(name, photo, isAvenger, description);
      superHeores.add(superHero);
      when(repository.getByName(name)).thenReturn(superHero);
    }
    when(repository.getAll()).thenReturn(superHeores);
    return superHeores;
  }

  private void givenThereAreNoSuperHeroes() {
    when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
  }

  private MainActivity startActivity() {
    return activityRule.launchActivity(null);
  }
}