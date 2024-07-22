import random
import database as db

def process_color_payload(payload: dict) -> dict:
    # TODO: Add further computation of payload here
            # (e.g. encryption of payload)
            # Afterwards return the processed payload
    return payload

def __fill_with_duplicates(colors: list, size: int) -> list:
    filledColors = []
    for i in range(0, size):
        filledColors.append(colors[i % len(colors)])
    return filledColors

def __fill_with_interpolates(colors: list, size: int) -> list:
    filledColors = colors
    while len(filledColors) < size:
        [color1, color2] = random.choices(colors, k=2)
        interpolatedColor = [
            min(color1[0] + color2[0], 255),
            min(color1[1] + color2[1], 255),
            min(color1[2] + color2[2], 255),
        ]
        filledColors.append(interpolatedColor)
    return filledColors

def fill_color_list(colors: list, size: int, strategy) -> list:
    if strategy == "duplicate":
        return __fill_with_duplicates(colors, size)
    if strategy == "interpolate":
        return __fill_with_interpolates(colors, size)

def map_color_tiles(friendId, deviceId, colors) -> dict:
    # TODO: Add more complex mapping algorithm
    #       For example by taking into account the position and orientation
    #       of tiles and the image

    device = db.find_device(friendId, deviceId)
    if device is None:
        print("Couldn't find device with id: ", deviceId)

    tiles = device["panelIds"]
    numTiles = len(tiles)
    if len(colors) < numTiles:
        # Different tactics for filling the color list can be applied
        # 2 Examples: Duplicating colors or interpolating colors
        colors = fill_color_list(colors, numTiles, "interpolate") # Change this line to user other tactics
    else:
        colors = colors[:numTiles]
    return dict(zip(tiles, colors))
