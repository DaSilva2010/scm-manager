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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PagedCollection, Link } from "@scm-manager/ui-types";
import { Button } from "./buttons";

type Props = WithTranslation & {
  collection: PagedCollection;
  onPageChange?: (p: string) => void;
};

class Paginator extends React.Component<Props> {
  isLinkUnavailable(linkType: string) {
    return !this.props.collection || !this.props.collection._links[linkType];
  }

  createAction = (linkType: string) => () => {
    const { collection, onPageChange } = this.props;
    if (onPageChange) {
      const link = collection._links[linkType] as Link;
      if (link && link.href) {
        onPageChange(link.href);
      }
    }
  };

  renderFirstButton() {
    return this.renderPageButton(1, "first");
  }

  renderPreviousButton() {
    const { t } = this.props;
    return this.renderButton("pagination-previous", t("paginator.previous"), "prev");
  }

  renderNextButton() {
    const { t } = this.props;
    return this.renderButton("pagination-next", t("paginator.next"), "next");
  }

  renderLastButton() {
    const { collection } = this.props;
    return this.renderPageButton(collection.pageTotal, "last");
  }

  renderPageButton(page: number, linkType: string) {
    return this.renderButton("pagination-link", page.toString(), linkType);
  }

  renderButton(className: string, label: string, linkType: string) {
    return (
      <Button
        className={className}
        label={label}
        disabled={this.isLinkUnavailable(linkType)}
        action={this.createAction(linkType)}
      />
    );
  }

  seperator() {
    return <span className="pagination-ellipsis">&hellip;</span>;
  }

  currentPage(page: number) {
    return <Button className="pagination-link is-current" label={"" + page} disabled={true} />;
  }

  pageLinks() {
    const { collection } = this.props;

    const links = [];
    const page = collection.page + 1;
    const pageTotal = collection.pageTotal;
    if (page > 1) {
      links.push(this.renderFirstButton());
    }
    if (page > 3) {
      links.push(this.seperator());
    }
    if (page > 2) {
      links.push(this.renderPageButton(page - 1, "prev"));
    }

    links.push(this.currentPage(page));

    if (page + 1 < pageTotal) {
      links.push(this.renderPageButton(page + 1, "next"));
    }
    if (page + 2 < pageTotal) links.push(this.seperator());
    //if there exists pages between next and last
    if (page < pageTotal) {
      links.push(this.renderLastButton());
    }
    return links;
  }
  render() {
    return (
      <nav className="pagination is-centered" aria-label="pagination">
        {this.renderPreviousButton()}
        {this.renderNextButton()}
        <ul className="pagination-list">
          {this.pageLinks().map((link, index) => {
            return <li key={index}>{link}</li>;
          })}
        </ul>
      </nav>
    );
  }
}
export default withTranslation("commons")(Paginator);
