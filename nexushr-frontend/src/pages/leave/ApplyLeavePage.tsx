import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import api from '../../api/axios';
import { leaveApi } from '../../api/leaveApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import type { LeaveType } from '../../types/leave';

interface FormData {
  leaveTypeId: string;
  startDate: string;
  endDate: string;
  reason: string;
}

export function ApplyLeavePage() {
  const { employeeId } = useEmployeeProfile();
  const navigate = useNavigate();

  const [leaveTypes, setLeaveTypes] = useState<LeaveType[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>();

  useEffect(() => {
    api
      .get('/leave-types')
      .then((response) => {
        setLeaveTypes(response.data);
      })
      .catch((error) => {
        console.error('Failed to load leave types', error);
        setError('Failed to load leave types');
      });
  }, []);

  const onSubmit = async (data: FormData) => {
    if (!employeeId) {
      setError('No employee profile is linked to your account. Please contact Admin.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      await leaveApi.apply(employeeId, data);
      navigate('/leaves');
    } catch (e: any) {
      setError(
        e?.response?.data?.message ??
          'Could not submit leave request'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">
        Apply for Leave
      </h1>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
          {error}
        </div>
      )}

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-4"
      >
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Leave Type
          </label>

          <select
            {...register('leaveTypeId', {
              required: 'Required',
            })}
            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select leave type</option>

            {leaveTypes.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name} ({t.code})
              </option>
            ))}
          </select>

          {errors.leaveTypeId && (
            <p className="mt-1 text-xs text-red-500">
              {errors.leaveTypeId.message}
            </p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Start Date"
            type="date"
            error={errors.startDate?.message}
            {...register('startDate', {
              required: 'Required',
            })}
          />

          <Input
            label="End Date"
            type="date"
            error={errors.endDate?.message}
            {...register('endDate', {
              required: 'Required',
            })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Reason
          </label>

          <textarea
            {...register('reason', {
              required: 'Required',
              minLength: {
                value: 10,
                message: 'Reason must be at least 10 characters',
              },
            })}
            rows={4}
            placeholder="Describe the reason for your leave request..."
            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />

          {errors.reason && (
            <p className="mt-1 text-xs text-red-500">
              {errors.reason.message}
            </p>
          )}
        </div>

        <div className="flex gap-3 pt-2">
          <Button type="submit" loading={loading}>
            Submit Request
          </Button>

          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/leaves')}
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}