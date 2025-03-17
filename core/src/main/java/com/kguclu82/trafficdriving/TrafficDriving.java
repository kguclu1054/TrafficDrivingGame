package com.kguclu82.trafficdriving;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Collections;

public class TrafficDriving extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture background;
    private Texture redCar;
    private Texture steeringWheel;
    private Texture[] vans;
    private float carX, carY;
    private float scale = 2.5f;

    private float steeringWheelX, steeringWheelY;
    private float steeringWheelWidth, steeringWheelHeight;
    private float touchX, touchY;

    private float[] vanX, vanY;
    private float[] vanSpeed;
    private final int numVans = 3;

    private boolean[] isVanVisible;
    private float[] vanTimers;

    private final float[] fixedVanYPositions = {50, 400, 800};

    private float steeringAngle = 0f;
    private final float maxSteeringAngle = 90f;

    private float backgroundX = 0;
    private float backgroundSpeed = 10f;

    private boolean[] hasCollided;
    private BitmapFont font;
    private boolean isGameOver = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("background.png");
        redCar = new Texture("redcar.png");
        steeringWheel = new Texture("direction.png");
        vans = new Texture[] {new Texture("van.png"), new Texture("van2.png"), new Texture("van3.png")};

        vanX = new float[numVans];
        vanY = new float[numVans];
        vanSpeed = new float[numVans];
        isVanVisible = new boolean[numVans];
        vanTimers = new float[numVans];

        hasCollided = new boolean[numVans];
        for (int i = 0; i < numVans; i++) {
            hasCollided[i] = false;
        }

        font = new BitmapFont();

        // Initial setup for vans
        initializeVans();

        // Car initial position
        carX = Gdx.graphics.getWidth() / 6;
        carY = 200;

        steeringWheelWidth = steeringWheel.getWidth() * 0.7f;
        steeringWheelHeight = steeringWheel.getHeight() * 0.7f;
        steeringWheelX = 20;
        steeringWheelY = 20;
    }

    @Override
    public void render() {
        for (boolean collided : hasCollided) {
            if (collided) {
                gameOver();
                return;
            }
        }

        batch.begin();

        // Draw background
        batch.draw(background, backgroundX, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(background, backgroundX + Gdx.graphics.getWidth(), 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundX -= backgroundSpeed;

        if (backgroundX <= -Gdx.graphics.getWidth()) {
            backgroundX = 0;
        }

        // Draw red car
        batch.draw(redCar, carX, carY, redCar.getWidth() * scale, redCar.getHeight() * scale);

        // Draw vans
        for (int i = 0; i < numVans; i++) {
            if (isVanVisible[i]) {
                TextureRegion vanRegion = new TextureRegion(vans[i]);
                vanRegion.flip(false, false);
                batch.draw(vanRegion, vanX[i], vanY[i], vans[i].getWidth() * scale * 0.7f, vans[i].getHeight() * scale * 0.7f);

                vanX[i] -= vanSpeed[i];

                // If a van goes off-screen, reset its position
                if (vanX[i] < -vans[i].getWidth() * scale * 0.5f) {
                    if (vanTimers[i] <= 0f) {
                        vanTimers[i] = 2f;
                    } else {
                        vanTimers[i] -= Gdx.graphics.getDeltaTime();
                    }

                    // Eğer zaman dolduysa, van'ı yeni bir Y konumuna atayarak yeniden getir
                    if (vanTimers[i] <= 0f) {
                        vanX[i] = Gdx.graphics.getWidth();
                        randomizeVanPosition(i);  // Yeniden rastgele bir y konumuna geçir
                        isVanVisible[i] = true;   // Görünürlüğünü de tekrar ayarla
                    }
                }


                // Collision detection
                float carCollisionWidth = redCar.getWidth() * scale * 0.8f;
                float carCollisionHeight = redCar.getHeight() * scale * 0.8f;

                float vanCollisionWidth = vans[i].getWidth() * scale * 0.7f * 0.8f;
                float vanCollisionHeight = vans[i].getHeight() * scale * 0.7f * 0.8f;

                if (checkCollision(carX, carY, carCollisionWidth, carCollisionHeight,
                    vanX[i], vanY[i], vanCollisionWidth, vanCollisionHeight)) {
                    hasCollided[i] = true;
                }
            }
        }

        // Draw steering wheel
        TextureRegion steeringWheelRegion = new TextureRegion(steeringWheel);
        batch.draw(steeringWheelRegion, steeringWheelX, steeringWheelY, steeringWheelWidth / 2, steeringWheelHeight / 2,
            steeringWheelWidth, steeringWheelHeight, 1, 1, steeringAngle);

        batch.end();

        // Handle user input for steering wheel
        if (Gdx.input.isTouched()) {
            touchX = Gdx.input.getX();
            touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (touchX >= steeringWheelX && touchX <= steeringWheelX + steeringWheelWidth &&
                touchY >= steeringWheelY && touchY <= steeringWheelY + steeringWheelHeight) {

                float deltaX = touchX - (steeringWheelX + steeringWheelWidth / 2);
                steeringAngle = Math.min(maxSteeringAngle, Math.max(-maxSteeringAngle, deltaX / 3));

                if (steeringAngle > 0) {
                    carY += 10;
                } else if (steeringAngle < 0) {
                    carY -= 10;
                }
            }
        }

        // Keep the car within bounds
        if (carY < 0) carY = 0;
        if (carY > Gdx.graphics.getHeight() - redCar.getHeight() * scale) {
            carY = Gdx.graphics.getHeight() - redCar.getHeight() * scale;
        }
    }

    private void gameOver() {
        batch.begin();

        font.setColor(1f, 0f, 0f, 1f);
        font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2 - 260, Gdx.graphics.getHeight() / 2);

        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "Click to Restart", Gdx.graphics.getWidth() / 2 -300 , Gdx.graphics.getHeight() / 2 + 300 );

        font.getData().setScale(6f);

        batch.end();

        if (Gdx.input.isTouched()) {
            resetGame();
        }
    }

    private void resetGame() {
        carX = Gdx.graphics.getWidth() / 6;
        carY = 200;
        backgroundX = 0;
        isGameOver = false;

        initializeVans();  // Reinitialize vans with random positions

        // Optional: Set other initial conditions, such as score, etc.
    }

    private void initializeVans() {
        for (int i = 0; i < numVans; i++) {
            vanX[i] = Gdx.graphics.getWidth() + i * 200;
            vanY[i] = fixedVanYPositions[i];
            vanSpeed[i] = 15f + i * 1.5f;
            isVanVisible[i] = false;
            vanTimers[i] = 0f;
            hasCollided[i] = false;
        }
        randomizeVisibleVans();
    }

    private void randomizeVanPosition(int i) {
        // Mevcut kullanılan Y konumlarını listele
        ArrayList<Float> usedPositions = new ArrayList<>();
        for (int j = 0; j < numVans; j++) {
            if (i != j && isVanVisible[j]) {
                usedPositions.add(vanY[j]);
            }
        }

        // Kullanılmayan Y konumlarını bul
        ArrayList<Float> availablePositions = new ArrayList<>();
        for (float pos : fixedVanYPositions) {
            if (!usedPositions.contains(pos)) {
                availablePositions.add(pos);
            }
        }

        // Eğer en az bir kullanılmayan pozisyon varsa, onu ata
        if (!availablePositions.isEmpty()) {
            Collections.shuffle(availablePositions);
            vanY[i] = availablePositions.get(0);
        }
    }


    private void randomizeVisibleVans() {
        // Öncelikle tüm van'ları görünmez yap
        for (int i = 0; i < numVans; i++) {
            isVanVisible[i] = false;
        }

        // Tüm van'ların indexlerini listeye al ve karıştır
        ArrayList<Integer> vanIndices = new ArrayList<>();
        for (int i = 0; i < numVans; i++) {
            vanIndices.add(i);
        }
        Collections.shuffle(vanIndices);

        // Rastgele seçilen 2 van'ı görünür yap
        for (int i = 0; i < 2; i++) {
            int selectedVan = vanIndices.get(i);
            isVanVisible[selectedVan] = true;
            randomizeVanPosition(selectedVan);
        }
    }




    private boolean checkCollision(float carX, float carY, float carWidth, float carHeight,
                                   float vanX, float vanY, float vanWidth, float vanHeight) {
        return carX < vanX + vanWidth && carX + carWidth > vanX && carY < vanY + vanHeight && carY + carHeight > vanY;
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        redCar.dispose();
        steeringWheel.dispose();
        for (Texture van : vans) {
            van.dispose();
        }
    }
}


