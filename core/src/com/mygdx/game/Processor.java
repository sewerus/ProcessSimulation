package com.mygdx.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Timer;

public class Processor {
	protected ArrayList<Task> processes = new ArrayList<Task>();
	protected ArrayList<Integer> waitingProcesses = new ArrayList<Integer>();
	private float timer, timeStep;
	private int border;
	private int x, y, width, height;
	private int method;
	private boolean isOn;
	private float timePart; // to Round Robin
	private float currentTimePart; // to Round Robin
	private int lastRun = -1; // default -1, to SJF and Round Robin
	private float timeToEnd;
	private int whatToDraw; // 0 - prepare tasks, 1 - which method, 3 - run

	public Processor() {
		timer = 0F;
		timeStep = 0.01F;
		border = 5;
		this.isOn = false;
		this.x = 50;
		this.width = Gdx.graphics.getWidth() - x;
		this.whatToDraw = 0;
	}

	private int askTwoOptions(String text) {
		TextListener textListener = new TextListener();
		Timer wait = new Timer();
		String input;
		int answer = 0;

		Gdx.input.getTextInput(textListener, text, "", "");
		while (textListener.text == null) {
			wait.delay(5);
		}
		input = textListener.text;
		textListener.text = null;

		while (answer == 0) {
			if ((input.equals("1") || input.equals("2")) && !input.isEmpty()) {
				answer = Integer.parseInt(input);
				break;
			} else {
				Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
				while (textListener.text == null) {
					wait.delay(5);
				}
				input = textListener.text;
				textListener.text = null;
			}
		}
		return answer;
	}

	private float askFloatNumber(String text) {
		TextListener textListener = new TextListener();
		Timer wait = new Timer();
		String input;
		float answer = -1F;
		Gdx.input.getTextInput(textListener, text, "", "");
		while (textListener.text == null) {
			wait.delay(5);
		}
		input = textListener.text;
		textListener.text = null;

		while (answer < 0) {
			while (true) {
				try {
					answer = Float.parseFloat(input);
					if (answer > 0) {
						break;
					} else {
						Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
						while (textListener.text == null) {
							wait.delay(5);
						}
						input = textListener.text;
						textListener.text = null;
					}
				} catch (NumberFormatException e) {
					Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
					while (textListener.text == null) {
						wait.delay(5);
					}
					input = textListener.text;
					textListener.text = null;
				}
			}
		}
		return answer;
	}

	private int askIntegerNumber(String text, int startFrom) {
		TextListener textListener = new TextListener();
		Timer wait = new Timer();
		String input;
		int answer = startFrom - 1;
		Gdx.input.getTextInput(textListener, text, "", "");
		while (textListener.text == null) {
			wait.delay(5);
		}
		input = textListener.text;
		textListener.text = null;

		while (answer < startFrom) {
			while (true) {
				try {
					answer = Integer.parseInt(input);
					if (answer >= startFrom) {
						break;
					} else {
						Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
						while (textListener.text == null) {
							wait.delay(5);
						}
						input = textListener.text;
						textListener.text = null;
					}
				} catch (NumberFormatException e) {
					Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
					while (textListener.text == null) {
						wait.delay(5);
					}
					input = textListener.text;
					textListener.text = null;
				}
			}
		}
		return answer;
	}

	protected void timeStep() {
		if (isOn) {
			timer += timeStep;
		}
	}

	private void randTasks() {
		Random generator = new Random();
		int processNumber = askIntegerNumber("Ile procesów?", 1);

		for (int i = 0; i < processNumber; i++) {
			processes.add(new Task(i, generator.nextInt(processNumber * 3),
					Math.abs(generator.nextInt(10) - generator.nextInt(5)) + 1));
		}
	}

	private void readTasks() {
		try {
			FileReader fileReader = new FileReader("data.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			int id = 0;
			while (line != null) {
				int endLogin = 0;

				while (Character.isDigit(line.charAt(endLogin))) {
					endLogin++;
				}

				// get timeStart
				int timeStart = Integer.parseInt(line.substring(0, endLogin));

				endLogin++;

				// get length
				int length = Integer.parseInt(line.substring(endLogin, line.length()));

				processes.add(new Task(id, timeStart, length));

				line = bufferedReader.readLine();
				id++;
			}
			bufferedReader.close();
		} catch (IOException e) {
			int answer = askTwoOptions("Niepowodzenie! 1 -> losuj, 2 -> zakoñcz");
			switch (answer) {
			case 1:
				randTasks();
				break;
			case 2:
				Gdx.app.exit();
			}
			e.printStackTrace();
		}
	}

	private void prepareTasks(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// readTasks
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 220, Gdx.graphics.getHeight() / 2 + 25, 210, 30, Color.BLUE,
				Color.BLUE, Color.BLUE, Color.BLUE);
		// randTasks
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + 10, Gdx.graphics.getHeight() / 2 + 25, 130, 30, Color.BLUE,
				Color.BLUE, Color.BLUE, Color.BLUE);
		shapeRenderer.end();

		font.setColor(Color.WHITE);
		batch.begin();
		// readTasks
		font.draw(batch, "WCZYTAJ DANE Z PLIKU [F]", Gdx.graphics.getWidth() / 2 - 210,
				Gdx.graphics.getHeight() / 2 + 45);
		// randTasks
		font.draw(batch, "LOSUJ DANE [R]", Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 + 45);
		batch.end();

		if (Gdx.input.isTouched()) {
			if (Gdx.input.getY() > Gdx.graphics.getHeight() / 2 - 55
					&& Gdx.input.getY() < Gdx.graphics.getHeight() / 2 - 25) {
				// readTasks
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 220
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 10) {
					readTasks();
					whatToDraw = 1;
					y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
					height = processes.size() * 20 + 25;
				}
				// randTasks
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 + 10
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 140) {
					randTasks();
					whatToDraw = 1;
					y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
					height = processes.size() * 20 + 25;
				}
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.F)) {
			readTasks();
			whatToDraw = 1;
			y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
			height = processes.size() * 20 + 25;
		}
		if (Gdx.input.isKeyJustPressed(Keys.R)) {
			randTasks();
			whatToDraw = 1;
			y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
			height = processes.size() * 20 + 25;
		}
	}

	private void addTask() {
		isOn = false;
		int answer = 0;

		answer = askTwoOptions("1 -> wpisz rêcznie, 2 -> losuj");

		switch (answer) {
		case 1:
			float length, timeStart;
			timeStart = 0F;
			length = 0F;

			timeStart = askFloatNumber("Podaj czas przyjscia");
			while (timeStart < timer) {
				timeStart = askFloatNumber("Ten moment juz minal!");
			}
			length = askFloatNumber("Podaj d³ugoœæ procesu");

			processes.add(new Task(processes.size(), timeStart, length));
			break;
		case 2:
			Random generator = new Random();
			processes.add(new Task(processes.size(), timer + generator.nextInt(processes.size() * 3),
					Math.abs(generator.nextInt(10) - generator.nextInt(5)) + 1));
			break;
		}
		height = processes.size() * 20 + 25;
		isOn = true;
	}

	private void whichMethod(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// FCFS
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 290, Gdx.graphics.getHeight() / 2 - 15, 90, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// SJF nw
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 190, Gdx.graphics.getHeight() / 2 - 15, 200, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// SJF wyw
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 - 15, 180, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// RoundRobin
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + 210, Gdx.graphics.getHeight() / 2 - 15, 125, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		shapeRenderer.end();

		font.setColor(Color.WHITE);
		batch.begin();
		// FCFS
		font.draw(batch, "FCFS [F]", Gdx.graphics.getWidth() / 2 - 275, Gdx.graphics.getHeight() / 2 + 5);
		// SJF nw
		font.draw(batch, "SJF niewywlaszczeniowy [S]", Gdx.graphics.getWidth() / 2 - 180, Gdx.graphics.getHeight() / 2 + 5);
		// SJF wyw
		font.draw(batch, "SJF wywlaszczeniowy [W]", Gdx.graphics.getWidth() / 2 + 30, Gdx.graphics.getHeight() / 2 + 5);
		// RoundRobin
		font.draw(batch, "Round Robin [R]", Gdx.graphics.getWidth() / 2 + 220, Gdx.graphics.getHeight() / 2 + 5);
		batch.end();

		if (Gdx.input.isTouched()) {
			if (Gdx.input.getY() < Gdx.graphics.getHeight() / 2 + 15
					&& Gdx.input.getY() > Gdx.graphics.getHeight() / 2 - 15) {
				// FCFS
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 290
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 200) {
					method = 1;
					whatToDraw = 2;
				}
				// SJF nw
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 190
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 10) {
					method = 2;
					whatToDraw = 2;
				}
				// SJF wyw
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 + 20
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 200) {
					method = 3;
					whatToDraw = 2;
				}
				// RoundRobin
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 + 210
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 335) {
					method = 4;
					whatToDraw = 2;
					timePart = askFloatNumber("Jaki kwant czasu?");
					currentTimePart = 0F;
				}
			}
		}
		if(Gdx.input.isKeyJustPressed(Keys.F)) {
			method = 1;
			whatToDraw = 2;
		}
		if(Gdx.input.isKeyJustPressed(Keys.S)) {
			method = 2;
			whatToDraw = 2;
		}
		if(Gdx.input.isKeyJustPressed(Keys.W)) {
			method = 3;
			whatToDraw = 2;
		}
		if(Gdx.input.isKeyJustPressed(Keys.R)) {
			method = 4;
			whatToDraw = 2;
			timePart = askFloatNumber("Jaki kwant czasu?");
			currentTimePart = 0F;
		}
	}

	// ----------------------------------
	// ----------------------- ALGORHYTMS
	private void FCFS() {
		for (int i = 0; i < processes.size(); i++) {
			if (processes.get(i).currentPosition(timer) == 0 && !waitingProcesses.contains(i)
					&& !processes.get(i).isDone(timeStep)) {
				waitingProcesses.add(i);
			}
		}

		if (!waitingProcesses.isEmpty()) {
			if (lastRun == -1) {
				processes.get(waitingProcesses.get(0)).addStartTime(timer);
			}
			lastRun = waitingProcesses.get(0);
			processes.get(waitingProcesses.get(0)).increaseProgress(timeStep);

			if (processes.get(waitingProcesses.get(0)).isDone(timeStep)) {
				waitingProcesses.remove(0);
			}

			if (!waitingProcesses.isEmpty()) {
				if (lastRun != waitingProcesses.get(0)) {
					processes.get(lastRun).addEndTime(timer);
					timeToEnd = timer;
					processes.get(waitingProcesses.get(0)).addStartTime(timer);
				}
			}
		} else if (lastRun != -1) {
			processes.get(lastRun).addEndTime(timer);
			timeToEnd = timer;
			lastRun = -1;
		}
	}

	private void SJF_1() {
		for (int i = 0; i < processes.size(); i++) {
			if (processes.get(i).currentPosition(timer) == 0 && !waitingProcesses.contains(i)
					&& !processes.get(i).isDone(timeStep)) {
				if (waitingProcesses.isEmpty()) {
					waitingProcesses.add(i);
				} else {
					// add this new process in the right place to the list keep
					// sorted
					if (waitingProcesses.size() > 1) {
						boolean wasAdded = false;
						for (int j = 1; j < waitingProcesses.size(); j++) {
							if (processes.get(waitingProcesses.get(j)).howMuchToEnd() > processes.get(i)
									.howMuchToEnd()) {
								waitingProcesses.add(j, i);
								wasAdded = true;
								break;
							}
						}
						if (!wasAdded) {
							waitingProcesses.add(i);
						}
					} else {
						waitingProcesses.add(i);
					}
				}
			}
		}

		if (!waitingProcesses.isEmpty()) {
			if (lastRun == -1) {
				processes.get(waitingProcesses.get(0)).addStartTime(timer);
			}
			lastRun = waitingProcesses.get(0);
			processes.get(waitingProcesses.get(0)).increaseProgress(timeStep);

			if (processes.get(waitingProcesses.get(0)).isDone(timeStep)) {
				waitingProcesses.remove(0);
			}

			if (!waitingProcesses.isEmpty()) {
				if (lastRun != waitingProcesses.get(0)) {
					processes.get(lastRun).addEndTime(timer);
					timeToEnd = timer;
					processes.get(waitingProcesses.get(0)).addStartTime(timer);
				}
			}
		} else if (lastRun != -1) {
			processes.get(lastRun).addEndTime(timer);
			timeToEnd = timer;
			lastRun = -1;
		}
	}

	private void SJF_2() {
		waitingProcesses.clear();
		for (int i = 0; i < processes.size(); i++) {
			if (processes.get(i).currentPosition(timer) == 0 && !processes.get(i).isDone(timeStep)) {
				if (waitingProcesses.isEmpty()) {
					waitingProcesses.add(i);
				} else {
					// add this new process in the right place to the list keep
					// sorted
					boolean wasAdded = false;
					for (int j = 0; j < waitingProcesses.size(); j++) {
						if (processes.get(waitingProcesses.get(j)).howMuchToEnd() > processes.get(i).howMuchToEnd()) {
							waitingProcesses.add(j, i);
							wasAdded = true;
							break;
						}
					}
					if (!wasAdded) {
						waitingProcesses.add(i);
					}
				}
			}
		}

		if (!waitingProcesses.isEmpty()) {
			if (lastRun != waitingProcesses.get(0)) {
				if (lastRun != -1) {
					processes.get(lastRun).addEndTime(timer);
					timeToEnd = timer;
				}
				processes.get(waitingProcesses.get(0)).addStartTime(timer);
			}
			lastRun = waitingProcesses.get(0);
			processes.get(waitingProcesses.get(0)).increaseProgress(timeStep);
		} else if (lastRun != -1) {
			processes.get(lastRun).addEndTime(timer);
			timeToEnd = timer;
			lastRun = -1;
		}
	}

	private void RoundRobin() {
		for (int i = 0; i < processes.size(); i++) {
			if (processes.get(i).currentPosition(timer) == 0 && !waitingProcesses.contains(i)
					&& !processes.get(i).isDone(timeStep)) {
				waitingProcesses.add(i);
			}
		}

		if (!waitingProcesses.isEmpty()) {
			currentTimePart += timeStep;

			if (lastRun == -1) {
				processes.get(waitingProcesses.get(0)).addStartTime(timer);
			}
			lastRun = waitingProcesses.get(0);
			processes.get(waitingProcesses.get(0)).increaseProgress(timeStep);

			if (currentTimePart >= timePart) {
				waitingProcesses.add(waitingProcesses.get(0));
				waitingProcesses.remove(0);
				currentTimePart = 0;
			}

			if (!waitingProcesses.isEmpty()) {
				if (processes.get(waitingProcesses.get(0)).isDone(timeStep)) {
					waitingProcesses.remove(0);
					currentTimePart = 0;
				}
			}

			if (!waitingProcesses.isEmpty()) {
				if (lastRun != waitingProcesses.get(0)) {
					processes.get(lastRun).addEndTime(timer);
					timeToEnd = timer;
					processes.get(waitingProcesses.get(0)).addStartTime(timer);
					currentTimePart = 0;
				}
			}
		} else if (lastRun != -1) {
			processes.get(lastRun).addEndTime(timer);
			timeToEnd = timer;
			lastRun = -1;
			currentTimePart = 0;
		}
	}

	// ----------------------- ALGORHYTMS
	// ----------------------------------

	protected void runProcess() {
		if (isOn) {
			switch (method) {
			case 1:
				FCFS();
				break;
			case 2:
				SJF_1();
				break;
			case 3:
				SJF_2();
				break;
			case 4:
				RoundRobin();
				break;
			}
		}
	}

	private void drawSimulation(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// border
		shapeRenderer.rect(x - border, y - border, width + 2 * border, height + 2 * border, Color.BROWN, Color.BROWN,
				Color.BROWN, Color.BROWN);
		// gate
		shapeRenderer.rect(x - border, y, border, height, Color.RED, Color.RED, Color.RED, Color.RED);
		// processor
		shapeRenderer.rect(x, y, width, height, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
		// tasks
		float averageWaitingTime = 0F;
		for (int i = 0; i < processes.size(); i++) {
			averageWaitingTime += processes.get(i).getAverageWaitingTime();
			int status = 0;
			if (!processes.get(i).isDone(timeStep)) {
				if (waitingProcesses.size() > 0) {
					if (waitingProcesses.get(0) == i) {
						status = 3;
					} else if (waitingProcesses.contains(i)) {
						status = 2;
					} else {
						status = 1;
					}
				} else {
					status = 1;
				}
				processes.get(i).draw(timer, processes.size(), x, y, status, shapeRenderer, batch, font);
			}
			processes.get(i).writeStatistics(processes.size(), status, shapeRenderer, batch, font);
		}
		averageWaitingTime /= processes.size();

		// end button
		shapeRenderer.rect(20, Gdx.graphics.getHeight() - (2 * processes.size() + 5) * 20 - 85, 190, 30, Color.RED,
				Color.RED, Color.RED, Color.RED);
		// statistics button
		shapeRenderer.rect(230, Gdx.graphics.getHeight() - (2 * processes.size() + 5) * 20 - 85, 240, 30, Color.BLUE,
				Color.BLUE, Color.BLUE, Color.BLUE);
		// start/pause button
		shapeRenderer.rect(490, Gdx.graphics.getHeight() - (2 * processes.size() + 5) * 20 - 85, 190, 30, Color.YELLOW,
				Color.YELLOW, Color.YELLOW, Color.YELLOW);
		// new task
		shapeRenderer.rect(700, Gdx.graphics.getHeight() - (2 * processes.size() + 5) * 20 - 85, 160, 30, Color.GREEN,
				Color.GREEN, Color.GREEN, Color.GREEN);

		shapeRenderer.end();

		// description
		font.setColor(Color.WHITE);
		batch.begin();
		font.draw(batch, "Proces", 20, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Czas przyjscia", 20 + 100, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Dlugosc procesu", 20 + 250, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Postep", 20 + 400, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Czas oczekiwania", 20 + 500, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Czas wykonywania od-do", 20 + 650, Gdx.graphics.getHeight() - processes.size() * 20 - 70);
		font.draw(batch, "Sredni czas oczekiwania to " + String.format("%.1f", averageWaitingTime), 20,
				Gdx.graphics.getHeight() - (2 * processes.size() + 2) * 20 - 70);
		font.draw(batch, "Czas:  " + String.format("%.1f", timer), 20,
				Gdx.graphics.getHeight() - (2 * processes.size() + 3) * 20 - 70);
		font.draw(batch, "Oczekujace procesy:  ", 300, Gdx.graphics.getHeight() - (2 * processes.size() + 3) * 20 - 70);
		for (int i = 1; i < waitingProcesses.size(); i++) {
			font.draw(batch, Integer.toString(waitingProcesses.get(i) + 1) + ", ", 450 + i * 20,
					Gdx.graphics.getHeight() - (2 * processes.size() + 3) * 20 - 70);
		}

		// end button
		font.draw(batch, "ZAKONCZ PROGRAM [E]", 30, Gdx.graphics.getHeight() - (2 * processes.size() + 4) * 20 - 85);
		// statistics button
		font.draw(batch, "GENERUJ WYNIKI DO PLIKU [G]", 240,
				Gdx.graphics.getHeight() - (2 * processes.size() + 4) * 20 - 85);
		// start/pause button
		font.draw(batch, "START/PAUSE [SPACJA]", 500, Gdx.graphics.getHeight() - (2 * processes.size() + 4) * 20 - 85);
		// new task
		font.draw(batch, "NOWY PROCES [N]", 710, Gdx.graphics.getHeight() - (2 * processes.size() + 4) * 20 - 85);

		batch.end();
	}

	protected void drawAndCheckButtons(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		switch (whatToDraw) {
		case 0:
			prepareTasks(shapeRenderer, batch, font);
			break;
		case 1:
			whichMethod(shapeRenderer, batch, font);
			break;
		default:
			drawSimulation(shapeRenderer, batch, font);
			checkRunButtons();
			break;
		}
	}

	private void generateStatistics() {
		String data = "RAPORT SYMULACJI DLA PONI¯SZYCH PROCESÓW W CHWILI " + String.format("%.1f", timer)
				+ System.getProperty("line.separator");
		switch (method) {
		case 1:
			data += "UZYTA METODA: FCFS" + System.getProperty("line.separator");
			break;
		case 2:
			data += "UZYTA METODA: SJF niewywalaszczeniowy" + System.getProperty("line.separator");
			break;
		case 3:
			data += "UZYTA METODA: SJF wywlaszczeniowy" + System.getProperty("line.separator");
			break;

		case 4:
			data += "UZYTA METODA: Round Robin" + System.getProperty("line.separator");
			break;
		}
		data += "Proces\tCzas przyjscia\tDlugosc procesu\tCzas oczekiwania\tPostep\tCzas wykonywania od-do"
				+ System.getProperty("line.separator");
		for (int i = 0; i < processes.size(); i++) {
			data += Integer.toString(processes.get(i).id()) + "\t\t"
					+ String.format("%.1f", processes.get(i).timeStart()) + "\t\t"
					+ String.format("%.1f", processes.get(i).length()) + "\t\t"
					+ String.format("%.1f", processes.get(i).getAverageWaitingTime()) + "\t\t"
					+ processes.get(i).getProgress() + "\t\t" + processes.get(i).getTimes()
					+ System.getProperty("line.separator");
		}

		FileHandle handle = Gdx.files.local("result.txt");
		handle.writeString(data, false);
	}

	protected void checkRunButtons() {
		if (Gdx.input.isTouched()) {
			if (Gdx.input.getY() < (2 * processes.size() + 5) * 20 + 85
					&& Gdx.input.getY() > (2 * processes.size() + 5) * 20 + 55) {
				// exit
				if (Gdx.input.getX() > 20 && Gdx.input.getX() < 210) {
					Gdx.app.exit();
				}
				// statistics
				if (Gdx.input.getX() > 230 && Gdx.input.getX() < 470) {
					generateStatistics();
					Timer timer = new Timer();
					timer.delay(500);
				}
				// start/pause
				if (Gdx.input.getX() > 490 && Gdx.input.getX() < 680) {
					isOn = !isOn;
					Timer timer = new Timer();
					timer.delay(500);
				}
				// new task
				if (Gdx.input.getX() > 700 && Gdx.input.getX() < 860) {
					addTask();
					y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
					height = processes.size() * 20 + 25;
				}
			}
		}
		// statistics - e
		if (Gdx.input.isKeyJustPressed(Keys.E)) {
			Gdx.app.exit();
		}
		// statistics - g
		if (Gdx.input.isKeyJustPressed(Keys.G)) {
			generateStatistics();
		}
		// start/pause - space
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			isOn = !isOn;
		}
		// new task - n
		if (Gdx.input.isKeyJustPressed(Keys.N)) {
			addTask();
			y = Gdx.graphics.getHeight() - processes.size() * 20 - 50;
		}
	}
}
