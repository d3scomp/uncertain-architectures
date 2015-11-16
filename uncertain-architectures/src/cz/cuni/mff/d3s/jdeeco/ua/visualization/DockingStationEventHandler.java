package cz.cuni.mff.d3s.jdeeco.ua.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.jdeeco.visualizer.extensions.DynamicEventHandler;
import cz.filipekt.jdcv.events.Event;
import cz.filipekt.jdcv.exceptions.InvalidAttributeValueException;
import cz.filipekt.jdcv.exceptions.TooManyEvents;
import cz.filipekt.jdcv.xml.Utils;

/**
 * SAX handler used to parse the XML file containing the docking station events.
 * 
 * @author Ilias Gerostathopoulos <iliasg@d3s.mff.cuni.cz>
 */
public class DockingStationEventHandler extends DynamicEventHandler {

	public DockingStationEventHandler() {
		// no specific starting or ending time for these events
		super(null, null);
	}

	/**
	 * Name of the event element
	 */
	private final String eventName = "event";

	/**
	 * Name of the type attribute of the event element
	 */
	private final String typeName = "eventType";

	/**
	 * Expected value of the type attribute of the event element. Events of
	 * different types should be ignored by this handler.
	 */
	private final String expectedTypeValue = DockingStationRecord.class.getCanonicalName();

	/**
	 * Name of the time attribute of the event element
	 */
	private final String timeName = "time";

	/**
	 * Name of the node sub-element of the event element
	 */
	private final String nodeName = "node";

	/**
	 * Flag to denote parsing a node element that is of the expected type
	 */
	private boolean visitingDockingStationNode = false;

	/**
	 * Flag to denote parsing a node sub-element
	 */
	private boolean visitingNodeNode = false;

	/**
	 * Storage for the parsed event elements
	 */
	private final List<Event> events = new ArrayList<>();

	/**
	 * Stack to keep reference to the parent event element
	 */
	private Stack<DockingStationEvent> dockingStationEventsStack = new Stack<>();

	/**
	 * @return The parsed event elements
	 * @see {@link DockingStationEventHandler#events}
	 */
	public List<Event> getEvents() {
		return events;
	}
	
	public String getEventType() {
		return DockingStationEvent.DOCKING_STATION_EVENT_TYPE;
	}

	/**
	 * Number of event elements encountered
	 */
	private long count = 0;

	/**
	 * Maximal allowed number of event elements in the selection
	 */
	private final long countLimit = 600_000L;

	/**
	 * When a docking station event element is encountered, it creates a corresponding
	 * object and pushes it to the stack. Also sets the flags according to the
	 * element that is encountered.
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equals(eventName)) {

			count += 1;
			if (count > countLimit) {
				String message = "The selection contains too many <event> elements. "
						+ "Please specify a selection of the log file which contains at most " + countLimit
						+ " elements.";
				throw new SAXException(new TooManyEvents(message));
			}

			String typeVal = attributes.getValue(typeName);
			if (typeVal.equals(expectedTypeValue)) {
				visitingDockingStationNode = true;
			} else {
				visitingDockingStationNode = false;
				return;
			}

			String timeVal = attributes.getValue(timeName);
			Utils.ensureNonNullAndNonEmptyAttr(eventName, timeName, timeVal);
			double time;
			try {
				time = Double.parseDouble(timeVal);
			} catch (NumberFormatException ex) {
				throw new SAXException(new InvalidAttributeValueException(
						"Time attribute of the docking station event must be in the \"double precision\" format."));
			}
			if (startAtConstraint && (startAtLimit > time)) {
				return;
			}
			if (endAtConstraint && (endAtLimit < time)) {
				return;
			}

			DockingStationEvent eev = new DockingStationEvent(time);
			dockingStationEventsStack.push(eev);
		} else {

			if (visitingDockingStationNode) {

				switch (qName) {

				case nodeName:
					visitingNodeNode = true;
					break;

				default:
					Log.i("Encountered unexpected sub-element of docking station event with qName: " + qName);
				}
			}
		}
	}

	/**
	 * When a closing event element (of the expected type) is encountered, the
	 * corresponding event element is popped from the stack and stored in the
	 * parsed form in the {@link DockingStationEventHandler#events} storage.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (visitingDockingStationNode && qName.equals(eventName)) {
			events.add(dockingStationEventsStack.pop());
			visitingDockingStationNode = false;
		}
	}

	/**
	 * Checks the flags to find what is being parsed, parses the (String)
	 * content of the XML element, and sets the corresponding attribute of the
	 * already generated event object. Also resets the flags.
	 */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (visitingDockingStationNode) {

			DockingStationEvent parent = dockingStationEventsStack.peek();
			String val = new String(ch, start, length);

			if (visitingNodeNode) {
				Utils.ensureNonNullAndNonEmptyAttr(eventName, nodeName, val);
				parent.setNode(val);
				visitingNodeNode = false;
				return;
			}
		}
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}

}
