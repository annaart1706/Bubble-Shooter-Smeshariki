package ru.samsung.gamestudio.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

import ru.samsung.gamestudio.GameSettings;
import ru.samsung.gamestudio.objects.GameObject;
import ru.samsung.gamestudio.objects.Smesharik;

public class ContactManager {

    World world;
    private ArrayList<Smesharik> ballsToFix = new ArrayList<Smesharik>();


    public ContactManager(World world) {
        this.world = world;

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if (fixA.getUserData() == null || fixB.getUserData() == null) {
                    return;
                }
                if (!(fixA.getUserData() instanceof Smesharik) || !(fixB.getUserData() instanceof Smesharik)) {
                    return;
                }

                int cDef = fixA.getFilterData().categoryBits;
                int cDef2 = fixB.getFilterData().categoryBits;

//                if ((cDef == GameSettings.FILTER_FLYING_BUBBLE && cDef2 == GameSettings.FILTER_FIXED_BUBBLES)
//                        || (cDef2 == GameSettings.FILTER_FLYING_BUBBLE && cDef == GameSettings.FILTER_FIXED_BUBBLES)) {
//
//                    if (cDef == GameSettings.FILTER_FLYING_BUBBLE && fixA.getUserData() instanceof Smesharik) {
//                        Smesharik flyingBall = (Smesharik) fixA.getUserData();
//                        ballsToFix.add(flyingBall);
//                    }
//                    else if(cDef2 == GameSettings.FILTER_FLYING_BUBBLE && fixB.getUserData() instanceof Smesharik){
//                        Smesharik flyingBall = (Smesharik) fixB.getUserData();
//                        ballsToFix.add(flyingBall);
//                    }
//
//                }



                // Проверяем, что ОБА столкнувшихся объекта — это Смешарики (а не стены)
                if (fixA.getUserData() instanceof Smesharik && fixB.getUserData() instanceof Smesharik) {

                    Smesharik ballA = (Smesharik) fixA.getUserData();
                    Smesharik ballB = (Smesharik) fixB.getUserData();

                    // Ситуация 1: Шар А летит, а Шар Б уже жестко зафиксирован в сетке
                    if (ballA.getCurrentState() == Smesharik.State.FLYING && ballB.getCurrentState() == Smesharik.State.FIXED) {
                        ballsToFix.add(ballA); // Замораживать нужно строго летящий шар А!
                    }

                    // Ситуация 2: Наоборот, Шар Б летит, а Шар А уже зафиксирован в сетке
                    else if (ballB.getCurrentState() == Smesharik.State.FLYING && ballA.getCurrentState() == Smesharik.State.FIXED) {
                        ballsToFix.add(ballB); // Замораживать нужно строго летящий шар Б!
                    }
                }

            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

    }
    public ArrayList<Smesharik> getBallsToFix(){
        return ballsToFix;
    }
    public void clearBallsToFix(){
        ballsToFix.clear();
    }

}
