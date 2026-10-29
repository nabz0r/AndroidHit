#!/usr/bin/env python3
"""Geometry-only audit regressions; no runtime assets or reports are modified."""
from pathlib import Path
import math
import runpy
import unittest

import numpy as np

audit=runpy.run_path(str(Path(__file__).with_name('audit-assets.py')))
validate=audit['validate_source_clip']
mask=audit['source_domain_mask']

class SourceClipTests(unittest.TestCase):
    def test_both_windings_and_collinear_forward_edges_are_valid(self):
        square=[[0,0],[4,0],[4,4],[0,4]]
        for polygon in (square,list(reversed(square)),[[0,0],[2,0],[4,0],[4,4],[0,4]]):
            with self.subTest(polygon=polygon):
                validate(polygon,[0,0,4,4])
                self.assertTrue(mask([0,0,4,4],polygon).all())

    def test_absent_clip_uses_the_exact_rectangle(self):
        self.assertTrue(mask([2,3,6,8]).all())
        self.assertEqual(mask([2,3,6,8]).shape,(5,4))

    def test_fractional_boundary_preserves_last_pixel_centre(self):
        actual=mask([10,20,14,24],[[10,20],[12.75,20],[12.75,22.75],[10,22.75]])
        expected=np.zeros((4,4),dtype=bool);expected[:3,:3]=True
        np.testing.assert_array_equal(actual,expected)

    def test_overlapping_bounding_rectangles_can_partition_pixels(self):
        first=mask([0,0,6,6],[[0,0],[6,0],[0,6]])
        second=mask([0,0,6,6],[[6,0],[6,6],[0,6]])
        coverage=first.astype(np.uint8)+second
        self.assertTrue(np.all(coverage==1))
        alpha=np.full((6,6),255,dtype=np.uint8)
        self.assertEqual(np.count_nonzero((alpha>=128)&(coverage==0)),0)
        self.assertEqual(np.count_nonzero((alpha>=128)&(coverage>1)),0)

    def test_domain_not_rectangle_detects_an_opaque_overlap_or_omission(self):
        left=mask([0,0,6,6],[[0,0],[3,0],[3,6],[0,6]])
        overlapping=mask([0,0,6,6],[[2,0],[6,0],[6,6],[2,6]])
        missing=mask([0,0,6,6],[[4,0],[6,0],[6,6],[4,6]])
        alpha=np.full((6,6),127,dtype=np.uint8);alpha[:,2:4]=128
        self.assertEqual(np.count_nonzero((alpha>=128)&(left.astype(np.uint8)+overlapping>1)),6)
        self.assertEqual(np.count_nonzero((alpha>=128)&(left.astype(np.uint8)+missing==0)),6)

    def test_invalid_coordinate_or_polygon_structures_are_rejected(self):
        invalid=[None,[],[[0,0],[4,4]],[[0,0],[4,0],[4,4],[0,0]],
                 [[0,0],[4,0],[math.nan,4]],[[0,0],[4,0],[math.inf,4]],
                 [[0,0],[4,0],[True,4]],[[0,0],[4,0],['1',4]],
                 [[0,0],[4,0],[4,4,0]],[[0,0],[4,0],[-1e-10,4]],
                 [[0,0],[4,0],[4.0000001,4]],[[0,0],[2,0],[4,0]],
                 [[0,0],[4,4],[0,4],[3,0]],
                 [[0,0],[4,0],[4,4],[2,0],[0,4]],
                 [[0,0],[4,0],[2,0],[4,4],[0,4]]]
        for polygon in invalid:
            with self.subTest(polygon=polygon):
                with self.assertRaises(ValueError):validate(polygon,[0,0,4,4])

    def test_vertex_limit_is_exact(self):
        def circle(count):
            return [[2+math.cos(2*math.pi*i/count),2+math.sin(2*math.pi*i/count)] for i in range(count)]
        self.assertEqual(len(validate(circle(256),[0,0,4,4])),256)
        with self.assertRaises(ValueError):validate(circle(257),[0,0,4,4])

if __name__=='__main__':unittest.main(verbosity=2)
