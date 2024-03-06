#!/usr/bin/env bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# Mkdocs requires Python to run.
# Install the packages: `pip install mkdocs-material`
# Build the api docs: `./gradlew dokkaHtmlMultiModule`
# Then run this script to prepare the docs for the website.
# Finally, run `mkdocs serve` to preview the site locally or `mkdocs build` to build the site.

set -ex

# Copy the changelog into the site, omitting the unreleased section
cat CHANGELOG.md \
 | grep -v '^## Unreleased' \
 | sed '/^## /,$!d' \
 > docs/changelog.md

# Add the jinja frontmatter to the index
cat > docs/index.md <<- EOM
---
hide:
  - toc        # Hide table of contents
---

EOM

# Copy the README into the index, omitting the docs link, license and fixing hrefs
cat README.md \
  | sed '/## License/Q' \
  | sed -z 's/## Documentation[a-zA-z .\n()/:]*//g' \
  | sed 's!https://ajalt.github.io/mordant/!/!g' \
  | sed 's!docs/img!img!g' \
  >> docs/index.md
