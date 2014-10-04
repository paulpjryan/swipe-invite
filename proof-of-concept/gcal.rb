require 'rubygems'
require 'google_calendar'
require 'sinatra'

#SAMPLE REQUEST: http://localhost:9393/event?title=Christmas&start_month=12&start_day=25&start_hour=0&start_min=0&end_hour=23&end_min=59

cal = Google::Calendar.new(:username => 'swipeinvitetest@gmail.com',
                           :password => 'swipeinvite1',
                           :app_name => 'swipeinvite-googlecalendar-integration')

get '/event' do
	event = cal.create_event do |e|

		#set title
		title = 'Some Event'

		if params[:title]
			title = params[:title]
		end

		e.title = title

		#set start time
		start_year = Time.now.year
		start_month = Time.now.month
		start_day = Time.now.day
		start_hour = Time.now.hour
		start_min = Time.now.min

		if params[:start_year]
			start_year = params[:start_year]
		end
		if params[:start_month]
			start_month = params[:start_month]
		end
		if params[:start_day]
			start_day = params[:start_day]
		end
		if params[:start_hour]
			start_hour = params[:start_hour]
		end
		if params[:start_min]
			start_min = params[:start_min]
		end

		e.start_time = Time.new(start_year, start_month, start_day, start_hour, start_min	)


		#set end time
		end_year = start_year
		end_month = start_month
		end_day = start_day
		end_hour = start_hour.to_i + 1 #default length for event is 1 hour
		end_min	 = start_min	

		if params[:end_year]
			end_year = params[:end_year]
		end
		if params[:end_month]
			end_month = params[:end_month]
		end
		if params[:end_day]
			end_day = params[:end_day]
		end
		if params[:end_hour]
			end_hour = params[:end_hour]
		end
		if params[:end_min]
			end_min	= params[:end_min]
		end

		e.end_time = Time.new(end_year, end_month, end_day, end_hour, end_min	)
  	end
end