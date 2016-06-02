# ExpandCollapseAnimator
Android animator of view's height whether it is an item of RecyclerView or ListView or any regular view. 

Key intents of that library are:

1. Fast (no layout steps, no onLayout(), no onMeasure() calls)

2. Loosely coupled, lightweight, minimal dependencies and ability to use with low level API

3. Animate any view whether it is a recyclerview's item or regular view

## Usage
It's just 2 classes:

a. VerticalClipLayout - add it to your xml

Control height with VerticalClipLayout.setClipCoef(0..1)

Clicks and drawing will be adjusted to provided coefficient. So you don't click an invisible element or get weird visual when animating.

b. ExpandCollapseAnimator - use it if you need to make common list behaviour:

while 1 item expands, all the other collapses.

// adds view to processing

ExpandCollapseAnimator.add(int position, VerticalClipLayout view) - adds view to processing

ExpandCollapseAnimator.remove(int position) - remove (for example, when view gone offscreen)

ExpandCollapseAnimator.setExpanding(int position) - to start expanding that view, all the other will start (or continue) collapsing

## RecyclerView Demo
You can find example code in project "app". If you run it on android device, you'll see a list of cards with different gradients (to simply distinguish them). When you click and item, it is expanded. Try it out.

![image](RecyclerViewDemo.png)

* RecyclerViewFragment - example code of how to setup ExpandCollapseAnimator library for RecyclerView

* RecyclerViewAdapter - adapter with example of how you can change item height programmatically

## License
It's free to use as long as you give credit to author.

Copyright 2014 Tokarev Mikhail (also known as Deepscorn)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
