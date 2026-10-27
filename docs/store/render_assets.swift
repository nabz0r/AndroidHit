#!/usr/bin/env swift
// Local SVG rasterization. Opaque feature export uses RGB; icon/Nova use RGBA.
import AppKit
import Foundation

struct Asset: Decodable { let stem: String; let width: Int; let height: Int; let alpha: Bool }
struct Specification: Decodable { let assets: [Asset] }
let directory = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
let specs = try JSONDecoder().decode(Specification.self, from: Data(contentsOf: directory.appendingPathComponent("asset-specs.json")))

func render(_ asset: Asset, width: Int? = nil, height: Int? = nil, output: String? = nil) throws {
    let w = width ?? asset.width, h = height ?? asset.height
    let source = directory.appendingPathComponent(asset.stem + ".svg")
    guard let image = NSImage(contentsOf: source) else { fatalError("Cannot read SVG: \(source.path)") }
    // Core Graphics draws into RGBA. Pack RGB bytes after drawing opaque assets.
    guard let bitmap = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: w, pixelsHigh: h, bitsPerSample: 8, samplesPerPixel: 4, hasAlpha: true, isPlanar: false, colorSpaceName: .deviceRGB, bytesPerRow: w * 4, bitsPerPixel: 32), let context = NSGraphicsContext(bitmapImageRep: bitmap) else { fatalError("Cannot create bitmap") }
    bitmap.bitmapData?.initialize(repeating: 0, count: w * h * 4)
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = context
    context.imageInterpolation = .high
    image.draw(in: NSRect(x: 0, y: 0, width: w, height: h), from: .zero, operation: .copy, fraction: 1)
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    let exported: NSBitmapImageRep
    if asset.alpha { exported = bitmap }
    else {
        guard let rgb = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: w, pixelsHigh: h, bitsPerSample: 8, samplesPerPixel: 3, hasAlpha: false, isPlanar: false, colorSpaceName: .deviceRGB, bytesPerRow: w * 3, bitsPerPixel: 24), let sourceBytes = bitmap.bitmapData, let rgbBytes = rgb.bitmapData else { fatalError("Cannot pack RGB") }
        for y in 0..<h { for x in 0..<w {
            let sourceOffset = y * bitmap.bytesPerRow + x * 4
            let destinationOffset = y * rgb.bytesPerRow + x * 3
            for channel in 0..<3 { rgbBytes[destinationOffset + channel] = sourceBytes[sourceOffset + channel] }
        } }
        exported = rgb
    }
    guard let data = exported.representation(using: .png, properties: [:]) else { fatalError("Cannot encode PNG") }
    let destination = directory.appendingPathComponent(output ?? asset.stem + ".png")
    try data.write(to: destination)
    print("Wrote \(destination.lastPathComponent): \(w) × \(h), \(data.count) bytes")
}
for asset in specs.assets { try render(asset) }
if let icon = specs.assets.first(where: { $0.stem == "luma-drift-icon" }) {
    try render(icon, width: 64, height: 64, output: "luma-drift-icon-64-preview.png")
}
