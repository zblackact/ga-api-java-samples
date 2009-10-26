/**
 * Copyright 2009 Google Inc.  All Rights Reserved.
 *
 * @fileoverview Implements a Protovis tree map visualization for a table of
 * Google Analytics landing pages and entrances. The tree is calculated by
 * from each directory of the lansing page url.
 *
 * @author api.nickm@google.com (Nick Mihailovski)
 */

// Protected namespace
if (!ga) {
  var ga = {};
}

/**
 * Main object for turning an HTML table into a protovis treemap visualization.
 */
ga.treemap = function() {
  var TABLE_ID = 'dataTable'; // Id of the div containing tabular data.
  var TREEMAP_ID = 'treemap'; // Id of the div containing the tree map.
  var TOOLTIP_ID = '#tooltip'; // Id of the tool tip div.
  var CONTROLS_ID = '#controls'; // Id of the div surrounding the ui controls.
  var INSTRUCTIONS_ID = '#instructions'; // Id of the instructions div.
  var SLIDER_ID = '#slider'; // Id of the slider div.
  var SLIDER_VAL_ID = '#sliderVal'; // Id of the div that has the slider value.
  var TT_TITLE_ID = '#tt-title'; // Id of the tooltip title div.
  // Max number of characters in the title before the title is concatenated.
  var TT_TITLE_MAX_CHAR = 55;
  var TT_BOUNCE_ID = '#tt-b';  // Id of the tooltip bounce text.
  var TT_ENTRANCES_ID = '#tt-e';  // Id of the tooltip entrances text.
  var TT_BR_ID = '#tt-br'; // Id of the tooltip bounce rate text.
  var COLOR_LINE = '#333'; // Color of the treemap outlines.
  var COLOR_HIGHLIGHT = '#ccc'; // Highlight color for mouseovers.
  var COLOR_RANGE_START = 'green'; // Start of treemap color gradient.
  var COLOR_RANGE_MIDDLE = 'yellow'; // Midle of the color range.
  var COLOR_RANGE_END = 'red';  // End of treemap color gradient.

  var vis; // The main visualization.
  var treemapData; // The data for the tree map
  var brArray = []; // An array holding the min, mean, max values.
  var maxEntrances = 0; // Maximum value of entrances.
  var tooltipWidth; // The width of the tooltip.
  var tooltipX = 0; // Tooltip X position.
  var tooltipY = 0; // Tooltip Y position.
  var titleOn = true; // Whether the title is being displayed.
  var tooltipOn = true; // Whether the tool tip is being displayed.
  // Whether the query parameters should be made into directories.
  var queryToDirs = false;

  /**
   * Initializes the treemap.
   */
  var init = function() {
    // SVG detection.
    if (!document.implementation.hasFeature(
        'http://www.w3.org/TR/SVG11/feature#BasicStructure', '1.1')) {
      suckerScreen();
      return;
    }
    makeTreemap();
    configureUiControls();
    configureTooltip();
  };

  /**
   * Displays kind message to user they should upgrade to a browser that
   * supports SVG.
   */
  var suckerScreen = function() {
    $(CONTROLS_ID + ' > ul, ' + INSTRUCTIONS_ID).hide();
    $('#' + TREEMAP_ID).html([
      '<p>Oh Noez! Your browser doesn\'t support SVG!!?</p>',
      '<p class="small-text">Instead of seeing a fancy treemap ',
      'visualization your browser only allows you to see the lame table ',
      'below.</p>',
      '<p class="small-text">Time to get with the program and upgrade to a ',
      'modern browser like ',
      '<a href="http://getfirefox.com">Firefox 3.5</a> ',
      '<a href="http://apple.com/safari">Safari</a> ',
      '<a href="http://www.opera.com/">Opera</a> or ',
      '<a href="http://google.com/chrome">Chrome</a> ',
      'to see this demo in it\'s full splendor!'
    ].join('')).css('text-align: center');
  };
  /**
   * Extracts the data from the table in a div with id ga.TABLE_ID and passes
   * it to the render function.
   */
  var makeTreemap = function() {
    var tree = getTreeFromHtmlTable(TABLE_ID);
    renderTreemap(TREEMAP_ID, tree);
    addMouseoverColor();
  };

  /**
   * Parses through the HTML table and converts all the string values to
   * integers. It also handles converting the query parameters into
   * directories as well as finding the min and max of entrances and
   * bounce rate. Any values that fall below the entrance threshold will be
   * filtered out. Finally it returns an object to be used by the treemap
   * visualization.
   * @param {string} tableId The id of the div surrounding the table.
   * @return {pv.tree} an object containing the data in the HTML table.
   */
  var getTreeFromHtmlTable = function(tableId) {
    var dataArray = [];
    var max = 0;
    var min = 0;
    brArray = []; // Clear the array.
    var filter = parseInt($(SLIDER_VAL_ID).text(), 10);

    // Put table data into an array.
    var tds = document.getElementById(tableId).getElementsByTagName('td');
    for (var i = 0; i < tds.length; i += 4) {
      var landingPage = $(tds[i]).text();

      // Convert landingPage query parameters into directories.
      if (queryToDirs) {
        landingPage = landingPage.split('?').join('/');
        landingPage = landingPage.split('&').join('/');
      }

      // Get max entrances to set slider.
      var entrances = parseInt($(tds[i + 1]).text(), 10);
      maxEntrances = (entrances > maxEntrances) ? entrances : maxEntrances;

      var bounces = parseInt($(tds[i + 2]).text(), 10);

      // Remove '%' from bounce rate and get the max & min for the table.
      var bounceRate = $(tds[i + 3]).text();
      bounceRate = parseFloat(bounceRate);
      if (i == 0) {
        max = min = bounceRate;
      } else {
        max = max > bounceRate ? max : bounceRate;
        min = min < bounceRate ? min : bounceRate;
      }

      // Only include data that is in the filter.
      if (filter == -1 || entrances <= filter) {
        dataArray.push({
          'landingPage': landingPage + '/leafNode',
          'entrances': entrances,
          'bounces': bounces,
          'bounceRate': bounceRate
        });
      }
    }
    // Put the min, mean, max bounceRates into the brArray.
    brArray.push(min, (min + max) / 2, max);

    // Return the data array as a tree object.
    return pv.tree(dataArray)
        .keys(function(d) { return d.landingPage.split('/'); })
        .value(function(d) {
          return {
            'landingPage': d.landingPage,
            'entrances': d.entrances,
            'bounces': d.bounces,
            'bounceRate' : d.bounceRate
          }
        })
        .map();
  };

  /**
   * Renders the treemap visualization in the specified div.
   * @param {string} treemapDiv The div to place the treemap.
   * @param {object} treeData The data to be rendered into a treemap.
   */
  var renderTreemap = function(treemapDiv, treeData) {
    var bounceColors = pv.Scale.linear.apply(this, brArray).range(
        COLOR_RANGE_START, COLOR_RANGE_MIDDLE, COLOR_RANGE_END);
    vis = new pv.Panel()
        .canvas(treemapDiv)
        .width(1000)
        .height(500);

    var treemapLayout = pv.Layout.treemap(treeData)
        .inset(0)
        .size(function(n) { return n.entrances; });

    var box = vis.add(pv.Bar)
        .extend(treemapLayout)
        .width(function(n) { return n.width; })
        .height(function(n) { return n.height; })
        .strokeStyle(function(n) {
          return n.data ? pv.color(COLOR_LINE) : null;
        })
        .lineWidth(1)
        .fillStyle(function(n) {
          return n.data ? bounceColors(n.data.bounceRate).alpha(0.6) : null;
        })
        .event('mouseover', function(n) {
          ga.treemap.updateTip(n);
        });

    var label = box.anchor('center')
        .add(pv.Label)
        .textStyle('#222')
        .textShadow('#555 0 0 0')
        .font('16px arial,sans-serif')
        .text(function(n) {
          return (n.depth == 2 && titleOn) ? n.keys.join('/') : '';
        });
    vis.render();
  };

  /**
   * Redraws the treemap.
   */
  var redrawTreemap = function() {
    vis.render();
  };

  /**
   * Configures the radio buttons for title visibility, converting query
   * parameters to directries, and setting up the slider control.
   */
  var configureUiControls = function() {
    // Add click handler to title radio buttons.
    $("input[name='radioTitles']").click(function() {
        titleOn = this.value == 'yes' ? true : false;
        redrawTreemap();
      });

    // Add click handlers to the query directory radio buttons.
    $("input[name='queryDirs']").click(function() {
        queryToDirs = this.value == 'yes' ? true : false;
        makeTreemap();
      });

    $(SLIDER_VAL_ID).text(maxEntrances);
    // Create the jQuery slider and it's click handlers.
    $(SLIDER_ID).slider({
      range: 'min',
      value: maxEntrances,
      min: 1,
      max: maxEntrances,
      slide: function(event, ui) {
        $(SLIDER_VAL_ID).text(ui.value);
      },
      stop: makeTreemap
    });
  };

  /**
   * Adds events to the tool tip when users roll over the treemap.
   */
  var configureTooltip = function() {
    var tooltip = $(TOOLTIP_ID);
    tooltipWidth = tooltip.width();

    $('#' + TREEMAP_ID)
      .mouseover(function() {
        tooltipOn = true;
        tooltip.show();
      })
      .mouseout(function() {
        tooltipOn = false;
        tooltip.hide();
      })
      .mousemove(function(e) {
        tooltipX = e.pageX;
        tooltipY = e.pageY;
        tooltip.hide();
        setTimeout(function() {
          if (e.pageX == tooltipX &&
              e.pageY == tooltipY && tooltipOn) {
            // calculate offset depending on where pointer is.
            var xOff = $(document).width() / 2 < e.pageX ?
                -15 - tooltipWidth : 10;
            tooltip.css({
              'top': e.pageY + 10,
              'left': e.pageX + xOff
            });
            tooltip.fadeIn('medium');
          }
        }, 40);
      });
  };

  /**
   * Updates the content of the tool tip with the data from the moused over
   * treemap node.
   * @param {object} n the node in the treemap the current user is moused over.
   */
  var updateTip = function(n) {
    // Remove the leafNode directory.
    var keys = n.data.landingPage.split('/');
    keys.pop();
    var title = keys.join('/');
    // Concatenate long titles to format "/xxx/ ... xxxx";
    if (title.length > TT_TITLE_MAX_CHAR) {
      var keepFirst = title.indexOf('/', 1);
      var removeLen = title.length - TT_TITLE_MAX_CHAR + keepFirst + 5;
      title = [
        title.substring(0, keepFirst),
        ' ... ',
        title.substring(removeLen, title.length)
      ].join('');
    }
    $(TT_TITLE_ID).text(title);
    $(TT_BOUNCE_ID).text(n.data.bounces);
    $(TT_ENTRANCES_ID).text(n.data.entrances);
    $(TT_BR_ID).text(n.data.bounceRate + '%');
  }

  /**
   * Handles changing the colors over each of the boxes in the tree map.
   */
  var addMouseoverColor = function() {
    $('#' + TREEMAP_ID + ' rect').mouseover(function() {
        $(this).data('fill', $(this).attr('fill'));
        $(this).attr('fill', COLOR_HIGHLIGHT);
      }).mouseout(function() {
        var x = $(this).data('fill');
        $(this).attr('fill', x);
      });
  };

  // Public functions.
  return {
    'init': init,
    'makeTreemap': makeTreemap,
    'redrawTreemap' : redrawTreemap,
    'updateTip': updateTip
  };
}();

