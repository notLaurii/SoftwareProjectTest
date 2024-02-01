package com.mygdx.game.world;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.EntityData;
//import com.mygdx.game.entities.EntityLoader;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.enemies.Slime;
import com.mygdx.game.entities.projectiles.BoomerangProjectile;
import com.mygdx.game.management.GameSaver;
import com.mygdx.game.weapons.rangedweapons.Boomerang;

public abstract class GameMap {
	
	public static Player player;
	public static ArrayList<Entity> entities;
	public static ArrayList<Entity> entitiesToRemove = new ArrayList<>();
	public static ArrayList<Entity> entitiesToAdd = new ArrayList<>();
	
	public GameMap() {
		entities = new ArrayList<Entity>();
		try {
			loadEntitiesFromJson("playerData.json", false);
		} catch (Exception e){
			loadEntitiesFromJson("defaultPlayerData.json", true);
		}
		player=(Player) entities.get(0);
		loadEntitiesFromJson("Map/Level1/entities.json", true);
	}
	
	public void render (OrthographicCamera camera, SpriteBatch batch) {
		for(Entity entity : entities) {
			entity.render(batch);
		}
	}
	public void update(float delta) {

	    for (Entity entity : entities) {
	        entity.update(delta, 9.81f);

	        // Überprüfe, ob die Gesundheit null ist und füge sie zur Liste der zu entfernenden Entitäten hinzu
	        if (entity.getHealth() <= 0 && entity.getHealth() < entity.getMaxHealth()) {
	            entitiesToRemove.add(entity);
	        }
	    }

	    // Entferne die Entitäten aus der Liste nach der Iteration
	    entities.removeAll(entitiesToRemove);
		entities.addAll(entitiesToAdd);
		entitiesToRemove.removeAll(entitiesToRemove);
		entitiesToAdd.removeAll(entitiesToAdd);
	}
	public abstract void dispose ();
	
	public TileType getTileTypeByLocation(int layer, float x, float y) {
		return this.getTileTypeByCoordinate(layer,  (int) (x / TileType.TILE_SIZE), (int) (y / TileType.TILE_SIZE));
	}
	
	
	public abstract TileType getTileTypeByCoordinate(int layer, int col, int row);
	
	public boolean doesEntityCollideWithMap(float x, float y, int width, int height) {
		if (x < 0 || y < 0 || x + width > getPixelWidth() || y + height > getPixelHeight()) 
			return true;
		
		for (int row = (int) (y/TileType.TILE_SIZE); row < Math.ceil((y + height) / TileType.TILE_SIZE); row++) {
			for (int col = (int) (x/TileType.TILE_SIZE); col < Math.ceil((x + width) / TileType.TILE_SIZE); col++) {
				for (int layer = 0; layer < getLayers(); layer++) {
					TileType type = getTileTypeByCoordinate(layer, col, row);
					if (type != null && type.isCollidable())
						return true;
				}
			}
		}
		
		return false;
	}
	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract int getLayers();
	
	public int getPixelWidth() {
		return this.getWidth() * TileType.TILE_SIZE;
	}
	public int getPixelHeight() {
		return this.getHeight() * TileType.TILE_SIZE;
	}
	 protected void loadEntitiesFromJson(String jsonFilePath, boolean internal) {
	        Json json = new Json();
		 ArrayList<EntityData> entityDataList = json.fromJson(ArrayList.class, EntityData.class, Gdx.files.local(jsonFilePath));
			if(internal)
	        entityDataList = json.fromJson(ArrayList.class, EntityData.class, Gdx.files.internal(jsonFilePath));
	        for (EntityData entityData : entityDataList) {
	            entities.add(createEntityFromData(entityData));
	        }
	    }

	    private Entity createEntityFromData(EntityData entityData) {
	        if ("Player".equals(entityData.type)) {
	            return new Player(entityData.id, entityData.x, entityData.y, this, entityData.maxHealth, entityData.attackDamage, entityData.speed, entityData.jumpVelocity, entityData.weaponID, entityData.skin);
	        } else if ("Slime".equals(entityData.type)) {
	            return new Slime(entityData.x, entityData.y, this, entityData.maxHealth, entityData.attackDamage, entityData.speed, entityData.jumpVelocity, entityData.weaponID);
	        }
		 else if ("Boomerang".equals(entityData.type)) {
		return new BoomerangProjectile(this, player, 5);
	}
	        return null;
	    }
	
}
