package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Task {
	private float timeStart;
	private float length;
	private float progress;
	private Color color;
	private int id;
	private String infoTime;
	private float waitingTime;
	private float endTime;

	public Task(int id, float timeStart, float length) {
		this.timeStart = timeStart;
		this.length = length;
		this.progress = 0F;
		this.color = new Color((int) (Math.random() * 0x1000000));
		this.id = id;
		this.infoTime = "";
		this.endTime = 0;
	}
	
	public int id() {
		return id;
	}
	
	protected float length() {
		return length;
	}
	
	protected float timeStart() {
		return timeStart;
	}

	protected float currentPosition(float time) {
		if (timeStart - time > 0) {
			return timeStart - time;
		} else {
			return 0;
		}
	}

	protected void increaseProgress(float x) {
		progress += x;
	}

	protected boolean isDone(float timeStep) {
		if (progress + timeStep >= length - timeStep) {
			progress = length;
			return true;
		} else {
			return false;
		}
	}

	protected float howMuchToEnd() {
		return length - progress;
	}

	protected void addStartTime(float time) {
		infoTime += " " + String.format("%.1f", time) + "-";
		if (endTime > 0) {
			waitingTime += time - endTime;
		} else {
			waitingTime += time - timeStart;
		}
	}

	protected void addEndTime(float time) {
		infoTime += String.format("%.1f", time);
		endTime = time;
	}

	protected float getAverageWaitingTime() {
		return waitingTime;
	}

	protected String getTimes() {
		return infoTime;
	}

	protected String getProgress() {
		return String.format("%.1f", progress / length * 100) + "%";
	}

	protected void draw(float time, int all, int x, int y, int status, ShapeRenderer shapeRenderer, SpriteBatch batch,
			BitmapFont font) {
		// body
		shapeRenderer.rect(x + currentPosition(time) * 50, y + 20 * (all - id - 1) + 15, howMuchToEnd() * 50, 15, color,
				color, color, color);
		// status
		switch (status) {
		case 1:
			font.setColor(Color.WHITE);
			break;
		case 2:
			font.setColor(Color.YELLOW);
			break;
		case 3:
			font.setColor(Color.GREEN);
			break;
		default:
			font.setColor(Color.GRAY);
			break;
		}
		// number
		batch.begin();
		font.draw(batch, Integer.toString(id + 1), 20, y + 20 * (all - id - 1) + 28);
		batch.end();
	}

	protected void writeStatistics(int all, int status, ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		// status
		switch (status) {
		case 1:
			font.setColor(Color.WHITE);
			break;
		case 2:
			font.setColor(Color.YELLOW);
			break;
		case 3:
			font.setColor(Color.GREEN);
			break;
		default:
			font.setColor(Color.GRAY);
			break;
		}
		batch.begin();
		font.draw(batch, Integer.toString(id + 1), 20, Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		font.draw(batch, String.format("%.1f", timeStart), 20 + 100,
				Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		font.draw(batch, String.format("%.1f", length), 20 + 250, Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		font.draw(batch, getProgress(), 20 + 400, Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		font.draw(batch, String.format("%.1f", getAverageWaitingTime()), 20 + 500,
				Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		font.draw(batch, getTimes(), 20 + 650, Gdx.graphics.getHeight() - (all + id + 1) * 20 - 70);
		batch.end();
	}
}
