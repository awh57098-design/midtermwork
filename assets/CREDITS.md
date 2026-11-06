# Asset credits

The Charmander (#004) and Rattata (#019) sprites are from the PokeAPI sprites collection:

- https://github.com/PokeAPI/sprites
- https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png
- https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/19.png

Pokemon character names and artwork belong to their respective rights holders, including Nintendo, Creatures Inc. and GAME FREAK. These are third-party reference assets for an educational fan-game prototype; no original-artwork ownership is claimed. Review the rights holders' terms before distributing beyond the course project.

The background, terrain and interface are rendered by the game's code. No assets from the course Project2.zip have been copied into this prototype.

Additional PokeAPI sprites used for evolution and ranged enemies:
- Charmeleon (#005): https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/5.png
- Charizard (#006): https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png
- Ekans (#023): https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/23.png

## Animation sources

Directional frame sheets and timing data for Charmander (#0004), Rattata (#0019) and Ekans (#0023):
https://github.com/PMDCollab/SpriteCollab

The original animation credits identify CHUNSOFT for the Idle, Walk, Attack, Hurt, Hop, Charge and Shoot animations used here. Exact original credit records are preserved at assets/animations/<id>/credits.txt; animation dimensions and durations remain in AnimData.xml. SpriteCollab's community reuse policy is non-commercial use with appropriate credit; underlying Pokemon character and game-art rights remain with their respective owners. The sheets are used unmodified and interpreted by the game renderer.

## Forest map art

The forest panorama and 4x4 terrain/prop atlas in assets/environment were generated specifically for this project with OpenAI's built-in image generation tool. They were not copied from a Pokemon game's map. The art direction uses a retro pixel-art woodland palette to accompany the credited character sprites. Rendering uses original collision coordinates; decorative props have no collisions. The original generated images are retained separately.

## Crystal Cavern map art

The cave panorama and cave 4x4 terrain/prop atlas in assets/environment/cave-background.png and cave-atlas.png were generated for this project with OpenAI's built-in image generation tool. The renderer reads the new sheets directly; the original generated bitmaps remain unchanged. Cave art depicts blue-violet caverns, stone ledges, crystals, sharp stone traps, signposts and a falling boulder.

## Boss characters and volcano art

Raticate (#0020), Arbok (#0024), and Magmar (#0126) directional animation sheets and AnimData.xml are from https://github.com/PMDCollab/SpriteCollab. Exact original credit records are preserved in each assets/animations/<id>/credits.txt; the used animation records identify CHUNSOFT.

The volcano panorama and terrain/prop atlas in assets/environment/volcano-background.png and volcano-atlas.png were generated specifically for this project with OpenAI's built-in image generation tool. The atlas was edited with that same tool to remove the empty background while preserving the game tiles. Source generated files are retained separately.

Item sprites: https://github.com/PokeAPI/sprites/tree/master/sprites/items (Pokemon assets owned by their respective rights holders). Multi Lens uses the zoom-lens icon. Item effects are customized for this student game.

Korean UI bitmap font: Noto Sans KR, Google / Adobe, SIL Open Font License 1.1. Generated from https://github.com/google/fonts/tree/main/ofl/notosanskr . License: fonts/OFL.txt. Rebuild: python tools/generate_korean_font.py path/to/NotoSansKR.ttf (Pillow required).

Move data (Scarlet/Violet): https://github.com/PokeAPI/api-data/blob/master/data/api/v2/pokemon/4/index.json . Korean names and move type/category/power: https://github.com/PokeAPI/pokeapi/tree/master/data/v2/csv . Cross-check: https://pokemondb.net/pokedex/charmander/moves/9 . Combat effects are adapted to this real-time platformer. TM sprite: PokeAPI sprites/items/tm-normal.png.

Modern 18-type chart: https://github.com/PokeAPI/pokeapi/blob/master/data/v2/csv/type_efficacy.csv . Bundled data: assets/moves/type-efficacy.csv.


### 기술 부가 효과 데이터
PokeAPI의 move_meta 및 move_meta_stat_changes 데이터를 참고했습니다. 게임용 사본: moves/move-effects.json. 턴제 수치 일부는 실시간 전투 규칙으로 변환했습니다.
- https://github.com/PokeAPI/pokeapi/blob/master/data/v2/csv/move_meta.csv
- https://github.com/PokeAPI/pokeapi/blob/master/data/v2/csv/move_meta_stat_changes.csv


### 종족치와 기술 시각 효과
- 종족치: PokeAPI API data, https://github.com/PokeAPI/api-data (pokemon 4, 5, 6, 19, 20, 23, 24, 126). 게임용 데이터: moves/species-stats.json.
- 새 기술 시각 효과는 ShapeRenderer로 직접 그리는 절차적 도형이며 외부 이펙트 이미지를 사용하지 않습니다.
