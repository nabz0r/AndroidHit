#!/usr/bin/env swift
// Rasterizes the original SVG sources with the macOS AppKit renderer.
import AppKit
import Foundation

let directory = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
for (name, width, height) in [("luma-drift-feature", 1024, 500), ("luma-drift-icon", 512, 512)] {
    let source = directory.appendingPathComponent(name + ".svg")
    guard let image = NSImage(contentsOf: source) else { fatalError("Cannot read SVG: \(source.path)") }
    guard let bitmap = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: width, pixelsHigh: height, bitsPerSample: 8, samplesPerPixel: 4, hasAlpha: true, isPlanar: false, colorSpaceName: .deviceRGB, bytesPerRow: width * 4, bitsPerPixel: 32), let context = NSGraphicsContext(bitmapImageRep: bitmap) else { fatalError("Cannot create bitmap") }
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = context
    image.draw(in: NSRect(x: 0, y: 0, width: width, height: height), from: .zero, operation: .copy, fraction: 1)
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    guard let data = bitmap.representation(using: .png, properties: [:]) else { fatalError("Cannot encode PNG") }
    let output = directory.appendingPathComponent(name + ".png")
    try data.write(to: output)
    print("Wrote \(output.lastPathComponent): \(width) × \(height), \(data.count) bytes")
}
