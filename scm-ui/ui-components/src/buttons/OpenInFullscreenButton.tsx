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
import * as React from "react";
import { FC, ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import FullscreenModal from "../modals/FullscreenModal";

type Props = {
  modalTitle: string;
  modalBody: ReactNode;
};

const Button = styled.a`
  width: 50px;
  &:hover {
    color: #33b2e8;
  }
`;

const OpenInFullscreenButton: FC<Props> = ({ modalTitle, modalBody }) => {
  const [t] = useTranslation("repos");
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <Button title={t("diff.fullscreen.open")} className="button" onClick={() => setShowModal(true)}>
        <i className="fas fa-search-plus" />
      </Button>
      <FullscreenModal
        title={modalTitle}
        closeFunction={() => setShowModal(false)}
        body={modalBody}
        active={showModal}
      />
    </>
  );
};

export default OpenInFullscreenButton;
