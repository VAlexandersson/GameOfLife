package com.snook;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MainView extends VBox {

    public static final int  EDITING = 0;
    public static final int  SIMULATING = 1;

    private InfoBar infoBar;
    private Toolbar toolbar;
    private Canvas canvas;
    private Affine affine;

    private Simulation simulation;
    private Simulation initialSimulation;

    private Simulator simulator;

    private int drawMode = Simulation.ALIVE;

    private int applicationState = EDITING;
    public MainView() {

        this.canvas = new Canvas(400, 400);
        this.canvas.setOnMousePressed(this::handleDraw);
        this.canvas.setOnMouseDragged(this::handleDraw);
        this.canvas.setOnMouseMoved(this::handledMoved);

        this.canvas.setOnKeyPressed(this::onKeyPressed);

        this.toolbar = new Toolbar(this);
        this.infoBar = new InfoBar();
        this.infoBar.setDrawMode(this.drawMode);
        this.infoBar.setCursorPosition(0, 0);

        Pane spacer = new Pane();
        spacer.setMinSize(0,0);
        spacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(toolbar, this.canvas, spacer, infoBar);

        this.affine = new Affine();
        this.affine.appendScale(400/10f, 400/10f);


        this.initialSimulation = new Simulation(10, 10);
        this.simulation = Simulation.copy(initialSimulation);
    }

    private void handledMoved(MouseEvent mouseEvent) {
        Point2D simCoordinate = this.getSimulationCoordinate(mouseEvent);
        this.infoBar.setCursorPosition((int)simCoordinate.getX(), (int)simCoordinate.getY());
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        System.out.println("Pressed key");
        if(keyEvent.getCode() == KeyCode.D) {
            this.setDrawMode(Simulation.ALIVE);
            //this.drawMode = Simulation.ALIVE;
            System.out.println("Draw mode");
        } else if(keyEvent.getCode() == KeyCode.E) {
            this.setDrawMode(Simulation.DEAD);
            //this.drawMode = Simulation.DEAD;
            System.out.println("Erase mode");
        }
    }

    private void handleDraw(MouseEvent mouseEvent) {

        if(applicationState == SIMULATING) {
            return;
        }
        Point2D simCoordinate = this.getSimulationCoordinate(mouseEvent);

        int simX = (int) simCoordinate.getX();
        int simY = (int) simCoordinate.getY();

        System.out.println(simX + ", " + simY);

        this.initialSimulation.setState(simX, simY, drawMode);

        draw();
    }

    private Point2D getSimulationCoordinate(MouseEvent mouseEvent) {
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();

        try {
            return this.affine.inverseTransform(mouseX, mouseY);
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException("Non invertible transform");
        }
    }

    public void draw() {
        GraphicsContext g = this.canvas.getGraphicsContext2D();
        g.setTransform(this.affine)
        ;
        g.setFill(Color.LIGHTGRAY);
        g.fillRect(0,0,450,450);

        if(this.applicationState == EDITING) {
            drawSimulation(this.initialSimulation);
        } else {
            drawSimulation(this.simulation);
        }

        g.setStroke(Color.GRAY);
        g.setLineWidth(0.05);
        for (int x = 0; x <= this.simulation.width; x++) {
            g.strokeLine(x, 0, x, 10);
        }
        for (int y = 0; y <= this.simulation.height; y++) {
            g.strokeLine(0,y,10,y);
        }
        canvas.requestFocus();
    }

    private void drawSimulation(Simulation simulationToDraw) {
        GraphicsContext g = this.canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        for (int x = 0; x < simulationToDraw.width; x++) {
            for (int y = 0; y < simulationToDraw.height; y++) {
                if (simulationToDraw.getState(x, y) == Simulation.ALIVE) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    public Simulation getSimulation() {
        return this.simulation;
    }

    public void setDrawMode(int newDrawMode) {
        this.drawMode = newDrawMode;
        this.infoBar.setDrawMode(newDrawMode);
    }

    public void setApplicationState(int applicationState) {
        if(applicationState == this.applicationState) {
            return;
        }
        if(applicationState == SIMULATING) {
            this.simulation = Simulation.copy(this.initialSimulation);
            this.simulator = new Simulator(this, this.simulation);
        }

        this.applicationState = applicationState;
        System.out.println("Application State: " + this.applicationState);
    }

    public Simulator getSimulator() {
        return simulator;
    }
}
