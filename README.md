# reddit-place
Parse the [16559897 tiles](https://www.reddit.com/r/redditdata/comments/6640ru/place_datasets_april_fools_2017/) placed on [r/place](https://reddit.com/r/place) over the course of 72 hours.

## Output
The following images were rendered with this program using the Place dataset.


### Final Render
![Final Render](render/place.bmp)

### Heatmap overlayed and color shifted
![Final Render w/ heatmap](render/place_render.bmp)

### 10 minute interval / 72 hour timelapse
![Timelapse](render/timelapse.gif)

## Running
Grab the [dataset](https://www.reddit.com/r/redditdata/comments/6640ru/place_datasets_april_fools_2017/) as a CSV. This was tested / ran against [this dataset](http://skeeto.s3.amazonaws.com/share/tile_placements_sorted.csv.xz).

Build and run this with the following JVM arguments
- dataset_location (path to the dataset.csv)
- image_location (path for the image output)
- heatmap_location (path for the heatmap output)

Takes about 2 minutes to load all rows, sort them, generate the bitmaps and write to the files. With everything in memory we could do some fun things like see the bitmap at specific times, etc. 