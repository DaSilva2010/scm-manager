/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import java.io.IOException;
import java.io.OutputStream;

class LineFilteredOutputStream extends OutputStream {
  private final OutputStream target;
  private final int start;
  private final Integer end;

  private Character lastLineBreakCharacter;
  private int currentLine = 0;

  LineFilteredOutputStream(OutputStream target, Integer start, Integer end) {
    this.target = target;
    this.start = start == null ? 0 : start;
    this.end = end == null ? Integer.MAX_VALUE : end;
  }

  @Override
  public void write(int b) throws IOException {
    switch (b) {
      case '\n':
      case '\r':
        if (lastLineBreakCharacter == null) {
          keepLineBreakInMind((char) b);
        } else if (lastLineBreakCharacter == b) {
          if (currentLine > start && currentLine <= end) {
            target.write('\n');
          }
          ++currentLine;
        } else {
          if (currentLine > start && currentLine <= end) {
            target.write('\n');
          }
          lastLineBreakCharacter = null;
        }
        break;
      default:
        if (lastLineBreakCharacter != null && currentLine > start && currentLine <= end) {
          target.write('\n');
        }
        lastLineBreakCharacter = null;
        if (currentLine >= start && currentLine < end) {
          target.write(b);
        }
    }
  }

  public void keepLineBreakInMind(char b) {
    lastLineBreakCharacter = b;
    ++currentLine;
  }

  @Override
  public void close() throws IOException {
    if (lastLineBreakCharacter != null && currentLine >= start && currentLine < end) {
      target.write('\n');
    }
    target.close();
  }
}
