package ru.samsung.gamestudio.managers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

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

                if (fixA.getUserData() instanceof Smesharik && fixB.getUserData() instanceof Smesharik) {

                    Smesharik ballA = (Smesharik) fixA.getUserData();
                    Smesharik ballB = (Smesharik) fixB.getUserData();

                    if (ballA.getCurrentState() == Smesharik.State.FLYING && ballB.getCurrentState() == Smesharik.State.FIXED) {
                        if (!ballsToFix.contains(ballA)) {
                            ballsToFix.add(ballA);
                        }
                    }
                    else if (ballB.getCurrentState() == Smesharik.State.FLYING && ballA.getCurrentState() == Smesharik.State.FIXED) {
                        if (!ballsToFix.contains(ballB)) {
                            ballsToFix.add(ballB);
                        }
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